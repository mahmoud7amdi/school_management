/** Student/parent portal: exam results for the chosen student. */
(function () {
    'use strict';

    const COLSPAN = 6;

    const tbody = document.getElementById('tableBody');
    const summary = document.getElementById('resultSummary');
    const picker = document.getElementById('childPicker');
    const pickerWrapper = document.getElementById('childPickerWrapper');

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    }

    /** Green at a pass, red at a fail, grey while unmarked. */
    function outcomeBadge(result) {
        if (result.absent) {
            return UI.badge('Absent', 'bg-secondary-subtle text-secondary-emphasis');
        }
        if (result.passed === null || result.passed === undefined) {
            return UI.badge('Not marked', 'bg-secondary-subtle text-secondary-emphasis');
        }
        return result.passed
            ? UI.badge('Passed', 'bg-success-subtle text-success-emphasis')
            : UI.badge('Failed', 'bg-danger-subtle text-danger-emphasis');
    }

    function renderStats(results) {
        const marked = results.filter((r) => r.percentage !== null && r.percentage !== undefined);
        setText('statTotal', results.length);
        setText('statPassed', results.filter((r) => r.passed === true).length);

        if (marked.length === 0) {
            setText('statAverage', '—');
            return;
        }
        const total = marked.reduce((sum, r) => sum + Number(r.percentage), 0);
        setText('statAverage', Math.round(total / marked.length) + '%');
    }

    function rowHtml(result) {
        const marks = result.absent
            ? UI.dash(null)
            : UI.escapeHtml((result.marksObtained !== null && result.marksObtained !== undefined
                ? result.marksObtained
                : '—') + ' / ' + (result.maxMarks !== null && result.maxMarks !== undefined
                ? result.maxMarks
                : '—'));

        return '<tr>' +
            '<td class="fw-semibold">' + UI.dash(result.exam ? result.exam.name : null) + '</td>' +
            '<td>' + marks + '</td>' +
            '<td>' + (result.percentage !== null && result.percentage !== undefined
                ? UI.escapeHtml(result.percentage + '%')
                : UI.dash(null)) + '</td>' +
            '<td>' + UI.dash(result.gradeLetter) + '</td>' +
            '<td>' + outcomeBadge(result) + '</td>' +
            '<td>' + UI.dash(result.remarks) + '</td>' +
            '</tr>';
    }

    function load(studentId) {
        UI.table.loading(tbody, COLSPAN, 'Loading results...');

        return Api.get(Portal.scopedUrl('/api/v1/portal/results', studentId))
            .then(function (results) {
                results = results || [];
                renderStats(results);

                if (results.length === 0) {
                    summary.textContent = 'No results published yet.';
                    UI.table.empty(tbody, COLSPAN,
                        'No exam results have been published yet.', 'ti-file-off');
                    return;
                }
                summary.textContent = results.length + ' result' + (results.length === 1 ? '' : 's');
                tbody.innerHTML = results.map(rowHtml).join('');
            })
            .catch(function (error) {
                summary.textContent = 'Could not load results.';
                UI.table.error(tbody, COLSPAN, error.message || 'Failed to load results.');
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
                UI.toast(error.message || 'Could not load results', 'danger');
            }
        });
})();
