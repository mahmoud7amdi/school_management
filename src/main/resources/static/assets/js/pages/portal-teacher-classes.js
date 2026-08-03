/** Teacher portal: the classes I teach, with a roster on demand. */
(function () {
    'use strict';

    const COLSPAN = 7;

    const tbody = document.getElementById('tableBody');
    const summary = document.getElementById('resultSummary');
    const rosterBody = document.getElementById('rosterBody');
    const rosterLabel = document.getElementById('rosterLabel');

    function rowHtml(item) {
        const subjects = (item.subjects || []).map((s) => s.name).join(', ');
        return '<tr>' +
            '<td class="fw-semibold">' + UI.escapeHtml(item.name || '') +
            (item.homeroom
                ? ' ' + UI.badge('Homeroom', 'bg-primary-subtle text-primary-emphasis')
                : '') +
            '</td>' +
            '<td>' + UI.dash(item.grade ? item.grade.name : null) + '</td>' +
            '<td>' + UI.dash(item.section ? item.section.name : null) + '</td>' +
            '<td>' + (subjects ? UI.escapeHtml(subjects) : UI.dash(null)) + '</td>' +
            '<td>' + UI.dash(item.roomNumber) + '</td>' +
            '<td>' + item.students + '</td>' +
            '<td class="text-end">' +
            '<button type="button" class="btn btn-sm btn-ghost-secondary" data-action="roster" ' +
            'data-id="' + item.id + '" data-name="' + UI.escapeHtml(item.name || '') + '" ' +
            'title="View roster"><i class="ti ti-users"></i></button> ' +
            '<a class="btn btn-sm btn-ghost-primary" title="Take register" ' +
            'href="/dashboard/attendance/register?classroomId=' + encodeURIComponent(item.id) + '">' +
            '<i class="ti ti-calendar-check"></i></a>' +
            '</td>' +
            '</tr>';
    }

    function load() {
        UI.table.loading(tbody, COLSPAN, 'Loading classes...');

        return Api.get('/api/v1/portal/teacher/classes')
            .then(function (classes) {
                if (!classes || classes.length === 0) {
                    summary.textContent = 'No classes assigned yet.';
                    UI.table.empty(tbody, COLSPAN,
                        'You have no classes yet. An administrator assigns these.', 'ti-door-off');
                    return;
                }
                const students = classes.reduce((total, item) => total + item.students, 0);
                summary.textContent = classes.length + ' class' + (classes.length === 1 ? '' : 'es')
                    + ' · ' + students + ' student' + (students === 1 ? '' : 's');
                tbody.innerHTML = classes.map(rowHtml).join('');
            })
            .catch(function (error) {
                summary.textContent = 'Could not load your classes.';
                UI.table.error(tbody, COLSPAN, error.message || 'Failed to load classes.');
            });
    }

    function showRoster(classroomId, className) {
        rosterLabel.textContent = 'Roster — ' + className;
        rosterBody.innerHTML = '<tr><td colspan="3" class="table-state">' +
            '<i class="ti ti-loader-2"></i>Loading roster...</td></tr>';
        bootstrap.Modal.getOrCreateInstance(document.getElementById('rosterModal')).show();

        Api.get('/api/v1/portal/teacher/classes/' + classroomId + '/roster')
            .then(function (students) {
                if (!students || students.length === 0) {
                    UI.table.empty(rosterBody, 3, 'No students placed in this class yet.', 'ti-users');
                    return;
                }
                rosterBody.innerHTML = students.map(function (student) {
                    return '<tr>' +
                        '<td class="fw-semibold">' + UI.escapeHtml(student.fullName || '') + '</td>' +
                        '<td>' + UI.dash(student.admissionNumber) + '</td>' +
                        '<td>' + UI.badge(student.statusLabel || '',
                            student.status === 'ACTIVE'
                                ? 'bg-success-subtle text-success-emphasis'
                                : 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
                        '</tr>';
                }).join('');
            })
            .catch(function (error) {
                UI.table.error(rosterBody, 3, error.message || 'Failed to load roster.');
            });
    }

    // Delegated: rows are replaced wholesale on every load.
    tbody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action="roster"]');
        if (button) {
            showRoster(button.getAttribute('data-id'), button.getAttribute('data-name'));
        }
    });

    Shell.requireRole('TEACHER')
        .then(load)
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load your classes', 'danger');
            }
        });
})();
