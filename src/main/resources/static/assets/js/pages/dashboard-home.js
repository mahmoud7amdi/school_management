/** Dashboard home: one call to /dashboard/stats fills every tile. */
(function () {
    'use strict';

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    }

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

    Shell.requireManager()
        .then(() => Api.get('/api/v1/dashboard/stats'))
        .then(function (stats) {
            setText('scopeLabel', stats.scopeLabel || 'your school');
            setText('statStudents', stats.students);
            setText('statUsers', stats.users);
            setText('statClassrooms', stats.classrooms);
            setText('statGrades', stats.grades);
            setText('statSubjects', stats.subjects);
            setText('statTeachers', stats.teachers);
            setText('statYears', stats.academicYears);
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
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load dashboard statistics', 'danger');
                const container = document.getElementById('gradeBreakdown');
                if (container) {
                    container.innerHTML = '<p class="text-danger mb-0">Failed to load.</p>';
                }
            }
        });
})();
