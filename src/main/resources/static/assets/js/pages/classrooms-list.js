/** Classrooms list, with a live occupancy bar per room. */
(function () {
    'use strict';

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
            '<td>' + UI.dash(classroom.academicYear ? classroom.academicYear.name : null) + '</td>' +
            '<td>' + UI.dash(classroom.classTeacherName) + '</td>' +
            '<td>' + occupancy(classroom) + '</td>' +
            '<td class="school-col">' + UI.dash(classroom.school ? classroom.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
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
        colspan: 7,
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
            classTeacherId: document.getElementById('classTeacherId').value || null
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [grades, years, teachers] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/users/teachers')
            ]);

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

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load classrooms', 'danger');
            }
        });
})();
