/** Payments list. The edit modal needs students and fee items. */
(function () {
    'use strict';

    const STATUS_CLASSES = {
        COMPLETED: 'bg-success-subtle text-success-emphasis',
        PENDING: 'bg-warning-subtle text-warning-emphasis',
        FAILED: 'bg-danger-subtle text-danger-emphasis',
        REFUNDED: 'bg-secondary-subtle text-secondary-emphasis'
    };

    let refreshFeeFilter = function () {};

    function money(value) {
        return Number(value || 0).toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function row(payment) {
        const studentName = UI.escapeHtml(payment.student ? payment.student.name : '—');
        const feeName = UI.escapeHtml(payment.feeStructure ? payment.feeStructure.name : '—');
        return '<tr>' +
            '<td class="fw-semibold">' + UI.escapeHtml(payment.receiptNumber || '—') + '</td>' +
            '<td>' +
                '<span class="fw-semibold">' + studentName + '</span>' +
                (payment.admissionNumber
                    ? '<div class="small text-secondary">' + UI.escapeHtml(payment.admissionNumber) + '</div>'
                    : '') +
            '</td>' +
            '<td>' + feeName + '</td>' +
            '<td class="fw-semibold">' + UI.escapeHtml(money(payment.amountPaid)) + '</td>' +
            '<td>' + UI.dash(UI.formatDate(payment.paymentDate)) + '</td>' +
            '<td>' + UI.dash(payment.methodLabel || payment.method) + '</td>' +
            '<td>' + UI.badge(payment.statusLabel || payment.status,
                STATUS_CLASSES[payment.status] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td class="school-col">' + UI.dash(payment.school ? payment.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + payment.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + payment.id + '" data-name="payment ' + UI.escapeHtml(payment.receiptNumber || '') +
                    '" title="Delete"><i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/fees/payments',
        label: 'payment',
        colspan: 9,
        emptyIcon: 'ti-receipt-off',
        emptyText: 'No payments recorded yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (payment) => [
            payment.receiptNumber,
            payment.student ? payment.student.name : null,
            payment.admissionNumber,
            payment.feeStructure ? payment.feeStructure.name : null,
            payment.methodLabel,
            payment.statusLabel,
            payment.school ? payment.school.name : null,
            String(payment.paymentDate)
        ],
        fillForm: function (payment) {
            document.getElementById('receiptNumber').value = payment.receiptNumber || '';
            document.getElementById('amountPaid').value = payment.amountPaid || '';
            document.getElementById('studentId').value = payment.student ? payment.student.id : '';
            refreshFeeFilter(payment.feeStructure ? payment.feeStructure.id : null);
            document.getElementById('paymentDate').value = payment.paymentDate || '';
            document.getElementById('method').value = payment.method || 'CASH';
            document.getElementById('status').value = payment.status || 'COMPLETED';
            document.getElementById('remarks').value = payment.remarks || '';
        },
        toPayload: () => ({
            receiptNumber: document.getElementById('receiptNumber').value.trim(),
            amountPaid: document.getElementById('amountPaid').value,
            paymentDate: document.getElementById('paymentDate').value,
            method: document.getElementById('method').value,
            status: document.getElementById('status').value,
            remarks: document.getElementById('remarks').value.trim() || null,
            studentId: document.getElementById('studentId').value,
            feeStructureId: document.getElementById('feeStructureId').value
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(async function () {
            const [studentList, feeList] = await Promise.all([
                Api.get('/api/v1/students'),
                Api.get('/api/v1/fees/structures')
            ]);

            UI.fillSelect(document.getElementById('studentId'), studentList, {
                placeholder: 'Select student',
                label: (student) => student.fullName + ' (' + student.admissionNumber + ')' +
                    (student.grade ? ' — ' + student.grade.name : '')
            });

            // Also registers the student change listener that narrows the picker.
            refreshFeeFilter = PaymentForm.bindFeeFilter({
                students: () => studentList,
                fees: () => feeList
            });

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load payments', 'danger');
            }
        });
})();
