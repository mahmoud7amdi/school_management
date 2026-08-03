/** Classrooms list, with a live occupancy bar per room. */
(function () {
    'use strict';

    // Sections belong to a grade, so the modal's picker is refiltered whenever the
    // grade changes rather than listing every section in the school.
    let allSections = [];

    function fillSectionsForGrade(selectedId) {
        const gradeId = document.getElementById('gradeId').value;
        const matching = allSections.filter((section) =>
            section.grade && String(section.grade.id) === String(gradeId));
        const select = document.getElementById('sectionId');
        UI.fillSelect(select, matching, {
            placeholder: 'No section',
            label: (section) => section.name
        });
        if (selectedId !== undefined) {
            select.value = selectedId === null ? '' : String(selectedId);
        }
    }

    function occupancy(classroom) {
        if (!classroom.capacity) {
            return UI.badge(classroom.studentCount + ' enrolled',
                'bg-secondary-subtle text-secondary-emphasis');
        }
        const pct = Math.min(Math.round((classroom.studentCount / classroom.capacity) * 100), 100);
        const full = classroom.studentCount >= classroom.capacity;
        return '<div class="d-flex align-items-center gap-2">' +
            '<span class="mini-bar-track" style="width:70px">' +
            '<span class="mini-bar-fill" style="width:' + pct + '%' +
                (full ? ';background-color:var(--ds-danger)' : '') + '"></span></span>' +
            '<span class="small' + (full ? ' text-danger fw-semibold' : '') + '">' +
            classroom.studentCount + '/' + classroom.capacity + '</span>' +
            '</div>';
    }

    function row(classroom) {
        const name = UI.escapeHtml(classroom.name);
        const roomTag = classroom.roomNumber
            ? '<div class="small text-secondary">Room ' + UI.escapeHtml(classroom.roomNumber) + '</div>'
            : '';
        return '<tr>' +
            '<td><span class="fw-semibold">' + name + '</span>' + roomTag + '</td>' +
            '<td>' + UI.dash(classroom.grade ? classroom.grade.name : null) + '</td>' +
            '<td>' + (classroom.section
                ? UI.badge(classroom.section.name, 'bg-info-subtle text-info-emphasis')
                : '<span class="text-secondary">&mdash;</span>') + '</td>' +
            '<td>' + UI.dash(classroom.academicYear ? classroom.academicYear.name : null) + '</td>' +
            '<td>' + UI.dash(classroom.classTeacherName) + '</td>' +
            '<td>' + occupancy(classroom) + '</td>' +
            '<td class="school-col">' + UI.dash(classroom.school ? classroom.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-secondary" data-action="staff" ' +
                    'data-id="' + classroom.id + '" data-name="' + name + '" ' +
                    'title="Teaching staff"><i class="ti ti-users fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + classroom.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + classroom.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/classrooms',
        label: 'classroom',
        colspan: 8,
        emptyIcon: 'ti-door-off',
        emptyText: 'No classrooms yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (classroom) => [
            classroom.name,
            classroom.roomNumber,
            classroom.classTeacherName,
            classroom.grade ? classroom.grade.name : null,
            classroom.section ? classroom.section.name : null,
            classroom.academicYear ? classroom.academicYear.name : null,
            classroom.school ? classroom.school.name : null
        ],
        fillForm: function (classroom) {
            document.getElementById('name').value = classroom.name || '';
            document.getElementById('capacity').value = classroom.capacity || '';
            document.getElementById('roomNumber').value = classroom.roomNumber || '';
            document.getElementById('gradeId').value = classroom.grade ? classroom.grade.id : '';
            document.getElementById('academicYearId').value =
                classroom.academicYear ? classroom.academicYear.id : '';
            fillSectionsForGrade(classroom.section ? classroom.section.id : null);
            // The teacher is matched by name: the list response carries the display
            // name rather than the id, and names are unique enough within a school.
            const teacherSelect = document.getElementById('classTeacherId');
            const match = Array.from(teacherSelect.options)
                .find((option) => option.textContent === classroom.classTeacherName);
            teacherSelect.value = match ? match.value : '';
        },
        toPayload: () => ({
            name: document.getElementById('name').value.trim(),
            capacity: document.getElementById('capacity').value || null,
            roomNumber: document.getElementById('roomNumber').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            academicYearId: document.getElementById('academicYearId').value,
            classTeacherId: document.getElementById('classTeacherId').value || null,
            sectionId: document.getElementById('sectionId').value || null
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    // --- teaching staff ----------------------------------------------------
    // Assignments are what grant a teacher portal access to a class, so they are managed
    // from the class itself rather than from a separate top-level page.

    let staffClassroomId = null;
    let allTeachers = [];   // Teacher records (not logins) — assignments point at these
    let allSubjects = [];

    function assignmentRow(assignment) {
        return '<tr>' +
            '<td class="fw-semibold">' + UI.dash(assignment.teacher ? assignment.teacher.name : null) + '</td>' +
            '<td>' + (assignment.subject
                ? UI.badge(assignment.subject.name, 'bg-primary-subtle text-primary-emphasis')
                : UI.badge('Whole class', 'bg-secondary-subtle text-secondary-emphasis')) + '</td>' +
            '<td class="text-end">' +
            '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="unassign" ' +
            'data-id="' + assignment.id + '" title="Remove"><i class="ti ti-trash"></i></button>' +
            '</td>' +
            '</tr>';
    }

    function loadAssignments() {
        const tbody = document.getElementById('staffBody');
        UI.table.loading(tbody, 3, 'Loading staff...');

        return Api.get('/api/v1/teaching-assignments' + Api.query({classroomId: staffClassroomId}))
            .then(function (assignments) {
                if (!assignments || !assignments.length) {
                    UI.table.empty(tbody, 3,
                        'No teachers assigned to this class yet.', 'ti-user-off');
                    return;
                }
                tbody.innerHTML = assignments.map(assignmentRow).join('');
            })
            .catch(function (error) {
                UI.table.error(tbody, 3, error.message || 'Failed to load staff.');
            });
    }

    function openStaffModal(classroomId, className) {
        staffClassroomId = classroomId;
        document.getElementById('staffLabel').textContent = 'Teaching staff — ' + className;

        // Only subjects in this class's grade are teachable here, matching the
        // server-side check. CrudPage exposes its loaded rows as a function.
        const classroom = page.items().find((item) => String(item.id) === String(classroomId));
        const gradeId = classroom && classroom.grade ? classroom.grade.id : null;
        const subjects = gradeId
            ? allSubjects.filter((s) => s.grade && String(s.grade.id) === String(gradeId))
            : allSubjects;

        UI.fillSelect(document.getElementById('assignTeacherId'), allTeachers, {
            placeholder: 'Select a teacher',
            label: (teacher) => teacher.fullName
        });
        UI.fillSelect(document.getElementById('assignSubjectId'), subjects, {
            placeholder: 'Whole class (no subject)',
            label: (subject) => subject.name
        });

        bootstrap.Modal.getOrCreateInstance(document.getElementById('staffModal')).show();
        loadAssignments();
    }

    document.getElementById('assignBtn').addEventListener('click', function () {
        const teacherId = document.getElementById('assignTeacherId').value;
        if (!teacherId) {
            UI.toast('Choose a teacher first.', 'warning');
            return;
        }
        const restore = UI.busy(this, 'Adding...');

        Api.post('/api/v1/teaching-assignments', {
            teacherId: teacherId,
            classroomId: staffClassroomId,
            subjectId: document.getElementById('assignSubjectId').value || null
        })
            .then(function () {
                UI.toast('Teacher assigned', 'success');
                restore();
                return loadAssignments();
            })
            .catch(function (error) {
                restore();
                UI.toast(error.message || 'Could not assign that teacher', 'danger');
            });
    });

    document.getElementById('staffBody').addEventListener('click', function (event) {
        const button = event.target.closest('[data-action="unassign"]');
        if (!button) {
            return;
        }
        Api.del('/api/v1/teaching-assignments/' + button.getAttribute('data-id'))
            .then(function () {
                UI.toast('Assignment removed', 'success');
                return loadAssignments();
            })
            .catch(function (error) {
                UI.toast(error.message || 'Could not remove that assignment', 'danger');
            });
    });

    // Delegated on the table, which CrudPage re-renders wholesale.
    document.getElementById('tableBody').addEventListener('click', function (event) {
        const button = event.target.closest('[data-action="staff"]');
        if (button) {
            openStaffModal(button.getAttribute('data-id'), button.getAttribute('data-name'));
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [grades, years, teachers, sections, teacherRecords, subjects] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/users/teachers'),
                Api.get('/api/v1/sections'),
                Api.get('/api/v1/teachers'),
                Api.get('/api/v1/subjects')
            ]);
            allSections = sections;
            allTeachers = teacherRecords;
            allSubjects = subjects;

            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            UI.fillSelect(document.getElementById('academicYearId'), years, {
                placeholder: 'Select academic year',
                label: (year) => showSchool && year.school
                    ? year.name + ' — ' + year.school.name
                    : year.name
            });
            UI.fillSelect(document.getElementById('classTeacherId'), teachers, {
                placeholder: 'No class teacher',
                label: (teacher) => teacher.fullName
            });

            document.getElementById('gradeId').addEventListener('change', () => fillSectionsForGrade());

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load classrooms', 'danger');
            }
        });
})();
