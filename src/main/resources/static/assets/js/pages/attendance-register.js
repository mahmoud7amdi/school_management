/**
 * Daily register — for admins and for the teachers who actually take it.
 *
 * Flow: choose a classroom + date, load the students in that class, paint the existing
 * marks for that day (or default to Present), edit inline, then save the whole register in
 * one call.
 *
 * The two roles reach the same grid by different routes: an admin lists every classroom
 * and builds the roster from enrollments, while a teacher lists only their own classes and
 * reads the roster from the portal endpoint. Everything below the pickers is shared.
 */
(function () {
    'use strict';

    const STATUSES = [
        {value: 'PRESENT', label: 'Present'},
        {value: 'ABSENT', label: 'Absent'},
        {value: 'LATE', label: 'Late'},
        {value: 'EXCUSED', label: 'Excused'},
        {value: 'HALF_DAY', label: 'Half Day'}
    ];

    // Attendance is keyed by student, not by day, so the register is rebuilt
    // from the chosen classroom's roster plus the saved marks for that date.
    let roster = [];          // students in the class
    let savedMarks = [];      // marks already on disk for (class, date)
    let allStudents = [];     // for matching marks to roster entries (admin path)
    let teacherClasses = [];  // teacher path: carries each class's subjects
    let teacherMode = false;

    const tbody = document.getElementById('registerBody');

    function statusSelect(value) {
        let html = '<select class="form-select form-select-sm status-select" aria-label="Status">';
        STATUSES.forEach(function (status) {
            html += '<option value="' + status.value + '"' +
                (status.value === value ? ' selected' : '') + '>' +
                status.label + '</option>';
        });
        return html + '</select>';
    }

    function renderRows() {
        if (!roster.length) {
            UI.table.empty(tbody, 4, 'No students are enrolled in this class yet.', 'ti-users-off');
            document.getElementById('markSummary').textContent = '';
            document.getElementById('saveRegisterBtn').disabled = true;
            return;
        }

        // Match saved marks back onto the roster by student id.
        const marksByStudent = {};
        savedMarks.forEach(function (mark) {
            marksByStudent[mark.student.id] = mark;
        });

        tbody.innerHTML = roster.map(function (student) {
            const mark = marksByStudent[student.id] || {};
            const name = UI.escapeHtml(student.fullName);
            return '<tr data-student="' + student.id + '">' +
                '<td class="fw-semibold">' + name + '</td>' +
                '<td class="text-secondary">' + UI.escapeHtml(student.admissionNumber || '') + '</td>' +
                '<td>' + statusSelect(mark.status || 'PRESENT') + '</td>' +
                '<td><input type="text" class="form-control form-control-sm remark-input" ' +
                    'maxlength="255" value="' + UI.escapeHtml(mark.remarks || '') +
                    '" placeholder="Optional note" /></td>' +
            '</tr>';
        }).join('');

        updateSummary();
        document.getElementById('saveRegisterBtn').disabled = false;
    }

    function updateSummary() {
        const counts = {};
        tbody.querySelectorAll('.status-select').forEach(function (select) {
            const value = select.value;
            counts[value] = (counts[value] || 0) + 1;
        });
        const parts = STATUSES.filter((status) => counts[status.value])
            .map((status) => status.label + ': ' + counts[status.value]);
        document.getElementById('markSummary').textContent = parts.join('  ·  ');
    }

    function buildPayload() {
        const entries = [];
        tbody.querySelectorAll('tr[data-student]').forEach(function (rowEl) {
            entries.push({
                studentId: rowEl.getAttribute('data-student'),
                status: rowEl.querySelector('.status-select').value,
                remarks: rowEl.querySelector('.remark-input').value.trim() || null
            });
        });
        return {
            classroomId: document.getElementById('classroomId').value,
            attendanceDate: document.getElementById('attendanceDate').value,
            subjectId: document.getElementById('subjectId').value || null,
            // Ignored for a teacher — the server attributes their register to them, so it
            // cannot be filed under a colleague's name.
            recordedById: null,
            entries: entries
        };
    }

    /**
     * Admin roster: the students enrolled (and still open) in this class for the current
     * year. Enrollments carry the classroom per year, so they are filtered client-side to
     * the open statuses — that way a repeating student still appears.
     */
    function loadRosterFromEnrollments() {
        const classroomId = document.getElementById('classroomId').value;
        return Api.get('/api/v1/enrollments').then(function (enrollments) {
            // Open statuses only — matches EnrollmentStatus.isOpen() on the server.
            const open = ['ENROLLED', 'REPEATING'];
            roster = enrollments
                .filter(function (e) {
                    return e.classroom && String(e.classroom.id) === String(classroomId)
                        && open.includes(e.status);
                })
                .map(function (e) {
                    // The enrollment names the student; the full student list fills
                    // in admission number and name.
                    const student = allStudents.find(function (s) {
                        return String(s.id) === String(e.student.id);
                    }) || {};
                    return {
                        id: e.student.id,
                        fullName: e.student.name || student.fullName,
                        admissionNumber: student.admissionNumber || ''
                    };
                });
        });
    }

    /** Teacher roster: read straight from the portal, which scopes to their own classes. */
    function loadRosterFromPortal() {
        const classroomId = document.getElementById('classroomId').value;
        return Api.get('/api/v1/portal/teacher/classes/' + encodeURIComponent(classroomId) + '/roster')
            .then(function (students) {
                roster = (students || []).map(function (student) {
                    return {
                        id: student.id,
                        fullName: student.fullName,
                        admissionNumber: student.admissionNumber || ''
                    };
                });
            });
    }

    /** A teacher's subject list is per class, so it is refreshed when the class changes. */
    function refreshTeacherSubjects() {
        if (!teacherMode) {
            return;
        }
        const classroomId = document.getElementById('classroomId').value;
        const selected = teacherClasses.find((item) => String(item.id) === String(classroomId));
        UI.fillSelect(document.getElementById('subjectId'), selected ? selected.subjects : [], {
            placeholder: 'Whole day',
            label: (subject) => subject.name
        });
    }

    async function loadRegister() {
        const classroomId = document.getElementById('classroomId').value;
        const date = document.getElementById('attendanceDate').value;
        if (!classroomId || !date) {
            UI.toast('Choose a classroom and a date first.', 'warning');
            return;
        }
        UI.table.loading(tbody, 4, 'Loading register...');
        try {
            await (teacherMode ? loadRosterFromPortal() : loadRosterFromEnrollments());
            const params = {classroomId: classroomId, date: date};
            savedMarks = await Api.get('/api/v1/attendance/register' + Api.query(params));
            renderRows();
            document.getElementById('registerMeta').textContent =
                'Classroom ' + document.getElementById('classroomId').selectedOptions[0].textContent +
                ' — ' + UI.formatDate(date);
        } catch (error) {
            UI.table.error(tbody, 4, error.message || 'Could not load the register.');
        }
    }

    document.getElementById('registerControls').addEventListener('submit', function (event) {
        event.preventDefault();
        loadRegister();
    });

    document.getElementById('classroomId').addEventListener('change', refreshTeacherSubjects);

    tbody.addEventListener('change', function (event) {
        if (event.target.classList.contains('status-select')) {
            updateSummary();
        }
    });

    document.getElementById('markAllPresentBtn').addEventListener('click', function () {
        tbody.querySelectorAll('.status-select').forEach(function (select) {
            select.value = 'PRESENT';
        });
        updateSummary();
    });

    document.getElementById('saveRegisterBtn').addEventListener('click', async function () {
        const btn = this;
        const restore = UI.busy(btn, 'Saving...');
        try {
            await Api.post('/api/v1/attendance/register', buildPayload());
            UI.toast('Register saved', 'success');
            await loadRegister();
        } catch (error) {
            UI.toast(error.message || 'Could not save the register', 'danger');
        } finally {
            restore();
        }
    });

    /** Deep link from the dashboard and the classes page: ?classroomId=. */
    function applyClassroomFromQuery() {
        const requested = new URLSearchParams(window.location.search).get('classroomId');
        if (!requested) {
            return false;
        }
        const select = document.getElementById('classroomId');
        const known = Array.prototype.some.call(select.options,
            (option) => option.value === requested);
        if (!known) {
            return false;
        }
        select.value = requested;
        refreshTeacherSubjects();
        return true;
    }

    Shell.requireRole(['SUPER_ADMIN', 'SCHOOL_ADMIN', 'TEACHER'])
        .then(async function () {
            teacherMode = Shell.isTeacher();

            if (teacherMode) {
                teacherClasses = await Api.get('/api/v1/portal/teacher/classes');
                UI.fillSelect(document.getElementById('classroomId'), teacherClasses, {
                    placeholder: 'Select classroom',
                    label: (classroom) => classroom.name +
                        (classroom.grade ? ' · ' + classroom.grade.name : '')
                });
                // Subjects come from the chosen class, so there is no separate fetch.
                refreshTeacherSubjects();

                if (!teacherClasses.length) {
                    UI.toast('You have no classes assigned yet. Ask an administrator.', 'warning');
                }
            } else {
                const [classrooms, students, subjects] = await Promise.all([
                    Api.get('/api/v1/classrooms'),
                    // The unpaged route: /api/v1/students is paged and returns an
                    // envelope, which has no .find() for the roster match below.
                    Api.get('/api/v1/students/all'),
                    Api.get('/api/v1/subjects')
                ]);
                allStudents = students;

                UI.fillSelect(document.getElementById('classroomId'), classrooms, {
                    placeholder: 'Select classroom',
                    label: (classroom) => classroom.name +
                        (classroom.grade ? ' · ' + classroom.grade.name : '')
                });
                UI.fillSelect(document.getElementById('subjectId'), subjects, {
                    placeholder: 'Whole day',
                    label: (subject) => subject.name
                });

                if (!classrooms.length) {
                    UI.toast('Create a classroom before taking attendance.', 'warning');
                }
            }

            document.getElementById('attendanceDate').value = new Date().toISOString().slice(0, 10);

            // Arriving with a class already chosen means the register can load itself.
            if (applyClassroomFromQuery()) {
                await loadRegister();
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the register page', 'danger');
            }
        });
})();
