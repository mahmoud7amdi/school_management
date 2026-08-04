/**
 * Platform reports: totals, an administrator-gap warning, and a row per school.
 *
 * Super admin only, via requireRole rather than requireAdmin: a school admin's reporting
 * is its own dashboard, already scoped to its school. Every figure here is an aggregate —
 * the endpoint returns counts per school and never a named person.
 */
(function () {
    'use strict';

    const COLSPAN = 6;
    const tbody = document.getElementById('reportTableBody');
    const tfoot = document.getElementById('reportTableFoot');
    const searchInput = document.getElementById('reportSearch');

    let schools = [];

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value === null || value === undefined ? '—' : value;
        }
    }

    function plural(count, word) {
        return count + ' ' + word + (count === 1 ? '' : 's');
    }

    function rowHtml(school) {
        const name = UI.escapeHtml(school.name);
        return '<tr>' +
            '<td>' +
                '<div class="d-flex align-items-center gap-2">' +
                    '<span class="avatar-initial">' + UI.escapeHtml(UI.initials(school.name)) + '</span>' +
                    '<div>' +
                        '<div class="fw-semibold">' + name + '</div>' +
                        '<div class="small text-secondary">' + UI.dash(school.email) + '</div>' +
                    '</div>' +
                '</div>' +
            '</td>' +
            // Zero admins is the one figure worth colouring: the school has nobody to run it.
            '<td class="text-end">' + (school.admins === 0
                ? UI.badge('None', 'bg-warning-subtle text-warning-emphasis')
                : school.admins) + '</td>' +
            '<td class="text-end">' + school.teachers + '</td>' +
            '<td class="text-end">' + school.students + '</td>' +
            '<td class="text-end">' + school.activeStudents + '</td>' +
            '<td>' + (school.active === false
                ? UI.badge('Inactive', 'bg-secondary-subtle text-secondary-emphasis')
                : UI.badge('Active', 'bg-success-subtle text-success-emphasis')) + '</td>' +
        '</tr>';
    }

    function renderTotals(totals) {
        setText('rpSchools', totals.schools);
        setText('rpSchoolsDetail', totals.activeSchools + ' active, ' + totals.inactiveSchools + ' inactive');
        setText('rpAdmins', totals.superAdmins + totals.schoolAdmins);
        setText('rpAdminsDetail',
            plural(totals.schoolAdmins, 'school admin') + ', ' + plural(totals.superAdmins, 'super admin'));
        setText('rpStudents', totals.students);
        setText('rpStudentsDetail', 'across ' + plural(totals.schools, 'school'));
        setText('rpTeachers', totals.teachers);
        setText('rpAccountsDetail', plural(totals.users, 'account') + ' in total');

        const alertEl = document.getElementById('rpAdminGapAlert');
        if (alertEl) {
            const gap = totals.schoolsWithoutAdmin;
            alertEl.classList.toggle('d-none', gap === 0);
            alertEl.classList.toggle('d-flex', gap > 0);
            if (gap > 0) {
                setText('rpAdminGapText',
                    plural(gap, 'school') + ' ' + (gap === 1 ? 'has' : 'have') + ' no administrator appointed.');
            }
        }
    }

    /** Footer totals come from the rows on screen, so they follow the search filter. */
    function renderFoot(list) {
        if (!tfoot) {
            return;
        }
        const sum = function (key) {
            return list.reduce(function (running, school) { return running + school[key]; }, 0);
        };
        tfoot.classList.toggle('d-none', list.length === 0);
        setText('rpFootAdmins', sum('admins'));
        setText('rpFootTeachers', sum('teachers'));
        setText('rpFootStudents', sum('students'));
        setText('rpFootActive', sum('activeStudents'));
    }

    function render(list) {
        const summary = document.getElementById('resultSummary');
        if (summary) {
            summary.textContent = schools.length
                ? plural(schools.length, 'school') + ' on the platform'
                : 'No schools registered yet';
        }
        renderFoot(list);
        if (!list.length) {
            UI.table.empty(tbody, COLSPAN,
                schools.length ? 'No schools match your search.' : 'No schools yet.', 'ti-building-off');
            return;
        }
        tbody.innerHTML = list.map(rowHtml).join('');
    }

    function filtered() {
        const term = (searchInput && searchInput.value || '').trim().toLowerCase();
        if (!term) {
            return schools;
        }
        return schools.filter(function (school) {
            return (school.name || '').toLowerCase().includes(term) ||
                (school.email || '').toLowerCase().includes(term);
        });
    }

    function load() {
        return Api.get('/api/v1/reports/platform').then(function (report) {
            schools = report.schools || [];
            renderTotals(report.totals);
            render(schools);
        });
    }

    if (searchInput) {
        searchInput.addEventListener('input', function () {
            render(filtered());
        });
    }

    const printBtn = document.getElementById('printReportBtn');
    if (printBtn) {
        printBtn.addEventListener('click', function () {
            window.print();
        });
    }

    Shell.requireRole('SUPER_ADMIN')
        .then(load)
        .catch(function (error) {
            // A failed role check has already redirected; only a load failure needs saying.
            if (error && error.message === 'Insufficient role') {
                return;
            }
            UI.toast((error && error.message) || 'Could not load reports', 'danger');
            UI.table.error(tbody, COLSPAN, 'Failed to load reports.');
        });
})();
