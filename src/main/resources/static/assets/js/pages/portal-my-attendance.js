/** Student/parent portal: attendance history for the chosen student. */
(function () {
    'use strict';

    const COLSPAN = 5;

    const tbody = document.getElementById('tableBody');
    const summary = document.getElementById('resultSummary');
    const picker = document.getElementById('childPicker');
    const pickerWrapper = document.getElementById('childPickerWrapper');

    const STATUS_CLASSES = {
        PRESENT: 'bg-success-subtle text-success-emphasis',
        ABSENT: 'bg-danger-subtle text-danger-emphasis',
        LATE: 'bg-warning-subtle text-warning-emphasis',
        EXCUSED: 'bg-info-subtle text-info-emphasis',
        HALF_DAY: 'bg-secondary-subtle text-secondary-emphasis'
    };

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    }

    function renderStats(records) {
        const counts = records.reduce(function (acc, record) {
            acc[record.status] = (acc[record.status] || 0) + 1;
            return acc;
        }, {});

        const present = counts.PRESENT || 0;
        setText('statPresent', present);
        setText('statAbsent', counts.ABSENT || 0);
        setText('statLate', counts.LATE || 0);
        // No register taken yet reads as a dash, not a misleading 0%.
        setText('statRate', records.length === 0
            ? '—'
            : Math.round((present * 100) / records.length) + '%');
    }

    function rowHtml(record) {
        return '<tr>' +
            '<td class="fw-semibold">' + UI.escapeHtml(UI.formatDate(record.attendanceDate) || '') + '</td>' +
            '<td>' + UI.badge(record.statusLabel || record.status || '',
                STATUS_CLASSES[record.status] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td>' + UI.dash(record.classroom ? record.classroom.name : null) + '</td>' +
            '<td>' + UI.dash(record.subject ? record.subject.name : null) + '</td>' +
            '<td>' + UI.dash(record.remarks) + '</td>' +
            '</tr>';
    }

    function load(studentId) {
        UI.table.loading(tbody, COLSPAN, 'Loading attendance...');

        return Api.get(Portal.scopedUrl('/api/v1/portal/attendance', studentId))
            .then(function (records) {
                records = records || [];
                renderStats(records);

                if (records.length === 0) {
                    summary.textContent = 'No attendance recorded yet.';
                    UI.table.empty(tbody, COLSPAN,
                        'No attendance has been recorded yet.', 'ti-calendar-off');
                    return;
                }
                summary.textContent = records.length + ' record' + (records.length === 1 ? '' : 's');
                tbody.innerHTML = records.map(rowHtml).join('');
            })
            .catch(function (error) {
                summary.textContent = 'Could not load attendance.';
                UI.table.error(tbody, COLSPAN, error.message || 'Failed to load attendance.');
            });
    }

    Portal.requireFamilyRole()
        .then(() => Portal.initStudentContext({
            picker: picker,
            pickerWrapper: pickerWrapper,
            onChange: load
        }))
        .then(function (context) {
            if (Shell.isParent() && context.children.length === 0) {
                summary.textContent = 'No children are linked to your account yet.';
                UI.table.empty(tbody, COLSPAN,
                    'No children are linked to your account. Contact the school office.', 'ti-users');
                return null;
            }
            return load(context.studentId);
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load attendance', 'danger');
            }
        });
})();
