/**
 * Student/parent portal: the fee ledger for the chosen student.
 *
 * Acknowledging is a read receipt, not a payment — the endpoint records who saw the
 * charge and moves no money. It is idempotent server-side, so a double click is harmless.
 */
(function () {
    'use strict';

    const COLSPAN = 8;

    const tbody = document.getElementById('tableBody');
    const summary = document.getElementById('resultSummary');
    const picker = document.getElementById('childPicker');
    const pickerWrapper = document.getElementById('childPickerWrapper');

    let currentStudentId = null;

    const SETTLEMENT_CLASSES = {
        PAID: 'bg-success-subtle text-success-emphasis',
        PARTIAL: 'bg-warning-subtle text-warning-emphasis',
        UNPAID: 'bg-danger-subtle text-danger-emphasis'
    };

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value;
        }
    }

    function money(value) {
        const number = Number(value || 0);
        return number.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
    }

    function renderStats(lines) {
        const billed = lines.reduce((sum, line) => sum + Number(line.amountDue || 0), 0);
        const paid = lines.reduce((sum, line) => sum + Number(line.amountPaid || 0), 0);
        setText('statBilled', money(billed));
        setText('statPaid', money(paid));
        setText('statBalance', money(billed - paid));
    }

    function rowHtml(line) {
        const feeId = line.feeStructure ? line.feeStructure.id : null;
        const settled = line.settlementStatus === 'PAID';

        return '<tr>' +
            '<td class="fw-semibold">' + UI.dash(line.feeStructure ? line.feeStructure.name : null) + '</td>' +
            '<td>' + UI.dash(line.feeTypeLabel) + '</td>' +
            '<td>' + UI.escapeHtml(UI.formatDate(line.dueDate) || '—') +
            (line.overdue ? ' ' + UI.badge('Overdue', 'bg-danger-subtle text-danger-emphasis') : '') +
            '</td>' +
            '<td>' + money(line.amountDue) + '</td>' +
            '<td>' + money(line.amountPaid) + '</td>' +
            '<td class="fw-semibold">' + money(line.balance) + '</td>' +
            '<td>' + UI.badge(line.settlementStatusLabel || '',
                SETTLEMENT_CLASSES[line.settlementStatus] || 'bg-secondary-subtle text-secondary-emphasis') +
            '</td>' +
            '<td class="text-end">' +
            (settled || !feeId
                ? ''
                : '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="ack" ' +
                'data-id="' + feeId + '" title="Mark as seen">' +
                '<i class="ti ti-eye-check me-1"></i>Acknowledge</button>') +
            '</td>' +
            '</tr>';
    }

    function load(studentId) {
        currentStudentId = studentId;
        UI.table.loading(tbody, COLSPAN, 'Loading fees...');

        return Api.get(Portal.scopedUrl('/api/v1/portal/fees', studentId))
            .then(function (lines) {
                lines = lines || [];
                renderStats(lines);

                if (lines.length === 0) {
                    summary.textContent = 'No fee items yet.';
                    UI.table.empty(tbody, COLSPAN,
                        'No fees have been set for this grade yet.', 'ti-cash-off');
                    return;
                }
                const due = lines.filter((line) => Number(line.balance) > 0).length;
                summary.textContent = lines.length + ' fee item' + (lines.length === 1 ? '' : 's')
                    + (due > 0 ? ' · ' + due + ' outstanding' : ' · all settled');
                tbody.innerHTML = lines.map(rowHtml).join('');
            })
            .catch(function (error) {
                summary.textContent = 'Could not load fees.';
                UI.table.error(tbody, COLSPAN, error.message || 'Failed to load fees.');
            });
    }

    // Delegated: rows are replaced wholesale on every load.
    tbody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action="ack"]');
        if (!button) {
            return;
        }
        const feeId = button.getAttribute('data-id');
        const restore = UI.busy(button, 'Saving...');

        const url = '/api/v1/portal/fees/' + encodeURIComponent(feeId) + '/acknowledge'
            + (currentStudentId ? Api.query({studentId: currentStudentId}) : '');

        Api.post(url, null)
            .then(function () {
                UI.toast('Fee acknowledged. This is a receipt only, not a payment.', 'success');
                restore();
                button.disabled = true;
            })
            .catch(function (error) {
                restore();
                UI.toast(error.message || 'Could not acknowledge that fee', 'danger');
            });
    });

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
                UI.toast(error.message || 'Could not load fees', 'danger');
            }
        });
})();
