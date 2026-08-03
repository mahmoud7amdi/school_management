/**
 * Dashboard home for every role.
 *
 * Dispatches on the signed-in role rather than guarding with requireManager(): this page
 * is where requireManager() sends people it turns away, so gating it would bounce a
 * teacher back to the page they are already on. Admins load /dashboard/stats; the three
 * portal roles load /portal/summary, which returns a role-shaped payload.
 */
(function () {
    'use strict';

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    }

    /** Em-dash for a missing number, so an empty tile never reads as a real zero. */
    function stat(value) {
        return value === null || value === undefined ? '—' : value;
    }

    function percent(value) {
        return value === null || value === undefined ? '—' : value + '%';
    }

    // --- admin ---------------------------------------------------------------

    /** Horizontal bars sized against the largest value in the set. */
    function renderGradeBreakdown(rows) {
        const container = document.getElementById('gradeBreakdown');
        if (!container) {
            return;
        }
        if (!rows || rows.length === 0) {
            container.innerHTML =
                '<div class="text-center text-secondary py-4">' +
                '<i class="ti ti-chart-bar d-block fs-1 opacity-50 mb-2"></i>' +
                'No students enrolled yet.</div>';
            return;
        }

        const max = rows.reduce((peak, row) => Math.max(peak, row.count), 0) || 1;
        container.innerHTML = rows.map(function (row) {
            const width = Math.round((row.count / max) * 100);
            return '<div class="mini-bar">' +
                '<span class="mini-bar-label" title="' + UI.escapeHtml(row.grade) + '">' +
                UI.escapeHtml(row.grade) + '</span>' +
                '<span class="mini-bar-track">' +
                '<span class="mini-bar-fill" style="width:' + width + '%"></span></span>' +
                '<span class="mini-bar-value">' + row.count + '</span>' +
                '</div>';
        }).join('');
    }

    function renderAdmin(stats) {
        setText('scopeLabel', stats.scopeLabel || 'your school');
        setText('statStudents', stat(stats.students));
        setText('statUsers', stat(stats.users));
        setText('statClassrooms', stat(stats.classrooms));
        setText('statGrades', stat(stats.grades));
        setText('statSubjects', stat(stats.subjects));
        setText('statTeachers', stat(stats.teachers));
        setText('statYears', stat(stats.academicYears));
        setText('statCurrentYear', stats.currentAcademicYear || 'Not set');

        if (stats.schools !== null && stats.schools !== undefined) {
            setText('statSchools', stats.schools);
        }
        // A super admin's "users" figure counts school admins, not everyone.
        if (Shell.isSuperAdmin()) {
            setText('peopleLabel', 'School Admins');
        }

        const active = document.getElementById('statActiveStudents');
        if (active && stats.students > 0) {
            active.textContent = stats.activeStudents + ' active';
        }

        renderGradeBreakdown(stats.studentsByGrade);
    }

    // --- teacher -------------------------------------------------------------

    function renderTeacher(summary) {
        const teacher = summary.teacher || {};
        setText('scopeLabel', summary.schoolName || 'your school');
        setText('tStatClasses', stat(teacher.classes));
        setText('tStatStudents', stat(teacher.students));
        setText('tStatSubjects', stat(teacher.subjects));
        setText('tStatNotes', stat(teacher.pendingAbsenceNotes));

        const tbody = document.getElementById('teacherClassesBody');
        if (!tbody) {
            return;
        }
        const classes = teacher.classList || [];
        if (classes.length === 0) {
            UI.table.empty(tbody, 5,
                'You have no classes yet. An administrator assigns these.', 'ti-door-off');
            return;
        }

        tbody.innerHTML = classes.map(function (row) {
            return '<tr>' +
                '<td class="fw-semibold">' + UI.escapeHtml(row.classroomName || '') + '</td>' +
                '<td>' + UI.dash(row.gradeName) + '</td>' +
                '<td>' + UI.dash(row.subjectNames) + '</td>' +
                '<td class="text-end">' + row.students + '</td>' +
                '<td class="text-end">' +
                '<a class="btn btn-sm btn-ghost-primary" href="/dashboard/attendance/register?classroomId=' +
                encodeURIComponent(row.classroomId) + '">' +
                '<i class="ti ti-calendar-check me-1"></i>Register</a>' +
                '</td>' +
                '</tr>';
        }).join('');
    }

    // --- student -------------------------------------------------------------

    function renderStudent(summary) {
        const student = summary.student || {};
        setText('scopeLabel', summary.schoolName || 'your school');
        setText('sStatAttendance', percent(student.attendanceRate));
        setText('sStatResults', stat(student.resultsPublished));
        setText('sStatFees', stat(student.outstandingFeeItems));
        setText('sStatNotes', stat(student.submittedAbsenceNotes));
        setText('sAdmissionNumber', UI.dash(student.admissionNumber));
        setText('sGrade', UI.dash(student.gradeName));
        setText('sClassroom', UI.dash(student.classroomName));

        const detail = document.getElementById('sStatAttendanceDetail');
        if (detail) {
            detail.textContent = student.attendanceRecorded
                ? student.daysPresent + ' of ' + student.attendanceRecorded + ' days present'
                : 'No register taken yet';
        }
        const passed = document.getElementById('sStatPassed');
        if (passed && student.resultsPublished) {
            passed.textContent = student.examsPassed + ' passed';
        }
    }

    // --- parent --------------------------------------------------------------

    function renderParent(summary) {
        const parent = summary.parent || {};
        setText('scopeLabel', summary.schoolName || 'your school');

        const tbody = document.getElementById('parentChildrenBody');
        if (!tbody) {
            return;
        }
        const children = parent.childList || [];
        if (children.length === 0) {
            UI.table.empty(tbody, 6,
                'No children are linked to your account yet. Contact the school office.', 'ti-users');
            return;
        }

        tbody.innerHTML = children.map(function (child) {
            return '<tr>' +
                '<td class="fw-semibold">' + UI.escapeHtml(child.studentName || '') + '</td>' +
                '<td>' + UI.dash(child.admissionNumber) + '</td>' +
                '<td>' + UI.dash(child.gradeName) + '</td>' +
                '<td>' + UI.dash(child.classroomName) + '</td>' +
                '<td class="text-end">' + percent(child.attendanceRate) + '</td>' +
                '<td class="text-end">' +
                (child.outstandingFeeItems > 0
                    ? UI.badge(child.outstandingFeeItems + ' due', 'bg-warning-subtle text-warning-emphasis')
                    : UI.badge('Clear', 'bg-success-subtle text-success-emphasis')) +
                '</td>' +
                '</tr>';
        }).join('');
    }

    // --- boot ----------------------------------------------------------------

    Shell.ready
        .then(function (user) {
            if (!user) {
                // No session: Shell has already redirected to /login.
                return null;
            }
            if (Shell.isManager()) {
                return Api.get('/api/v1/dashboard/stats').then(renderAdmin);
            }
            return Api.get('/api/v1/portal/summary').then(function (summary) {
                if (summary.role === 'TEACHER') {
                    renderTeacher(summary);
                } else if (summary.role === 'STUDENT') {
                    renderStudent(summary);
                } else {
                    renderParent(summary);
                }
            });
        })
        .catch(function (error) {
            UI.toast(error.message || 'Could not load your dashboard', 'danger');

            // Leave every loading placeholder in a terminal state rather than spinning.
            ['gradeBreakdown'].forEach(function (id) {
                const container = document.getElementById(id);
                if (container) {
                    container.innerHTML = '<p class="text-danger mb-0">Failed to load.</p>';
                }
            });
            [['teacherClassesBody', 5], ['parentChildrenBody', 6]].forEach(function (pair) {
                const tbody = document.getElementById(pair[0]);
                if (tbody) {
                    UI.table.error(tbody, pair[1], 'Failed to load.');
                }
            });
        });
})();
