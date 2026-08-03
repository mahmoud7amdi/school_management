/**
 * Attendance records — a read-and-delete view over every mark.
 *
 * Editing a single mark is deliberately not offered here: corrections belong on
 * the register page, where the whole day is visible at once.
 */
(function () {
    'use strict';

    const STATUS_CLASSES = {
        PRESENT: 'bg-success-subtle text-success-emphasis',
        ABSENT: 'bg-danger-subtle text-danger-emphasis',
        LATE: 'bg-warning-subtle text-warning-emphasis',
        EXCUSED: 'bg-info-subtle text-info-emphasis',
        HALF_DAY: 'bg-secondary-subtle text-secondary-emphasis'
    };

    function row(record) {
        const studentName = UI.escapeHtml(record.student ? record.student.name : '—');
        return '<tr>' +
            '<td>' + UI.dash(UI.formatDate(record.attendanceDate)) + '</td>' +
            '<td class="fw-semibold">' + studentName + '</td>' +
            '<td>' + UI.dash(record.classroom ? record.classroom.name : null) +
                (record.subject
                    ? '<div class="small text-secondary">' + UI.escapeHtml(record.subject.name) + '</div>'
                    : '') +
            '</td>' +
            '<td>' + UI.badge(record.statusLabel || record.status,
                STATUS_CLASSES[record.status] || 'bg-secondary-subtle text-secondary-emphasis') +
                (record.remarks
                    ? '<div class="small text-secondary">' + UI.escapeHtml(record.remarks) + '</div>'
                    : '') +
            '</td>' +
            '<td>' + UI.dash(record.recordedBy ? record.recordedBy.name : null) + '</td>' +
            '<td class="school-col">' + UI.dash(record.school ? record.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + record.id + '" data-name="this attendance mark" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/attendance',
        label: 'attendance record',
        colspan: 7,
        emptyIcon: 'ti-calendar-off',
        emptyText: 'No attendance recorded yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        row: row,
        searchFields: (record) => [
            record.student ? record.student.name : null,
            record.classroom ? record.classroom.name : null,
            record.subject ? record.subject.name : null,
            record.statusLabel,
            record.remarks,
            record.attendanceDate,
            record.school ? record.school.name : null
        ],
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(() => page.load())
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load attendance', 'danger');
            }
        });
})();
