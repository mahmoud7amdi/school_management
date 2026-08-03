/** Record payment. The school comes from the student; the fee must match their grade. */
(function () {
    'use strict';

    let students = [];
    let fees = [];

    UI.bindForm(document.getElementById('feePaymentForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/fees/payments', {
            receiptNumber: document.getElementById('receiptNumber').value.trim(),
            amountPaid: document.getElementById('amountPaid').value,
            paymentDate: document.getElementById('paymentDate').value,
            method: document.getElementById('method').value,
            status: document.getElementById('status').value,
            remarks: document.getElementById('remarks').value.trim() || null,
            studentId: document.getElementById('studentId').value,
            feeStructureId: document.getElementById('feeStructureId').value
        }),
        onSuccess: function () {
            UI.toast('Payment recorded', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/fees/payments';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const [studentList, feeList] = await Promise.all([
                Api.get('/api/v1/students'),
                Api.get('/api/v1/fees/structures')
            ]);
            students = studentList;
            fees = feeList;

            UI.fillSelect(document.getElementById('studentId'), students, {
                placeholder: 'Select student',
                label: (student) => student.fullName + ' (' + student.admissionNumber + ')' +
                    (student.grade ? ' — ' + student.grade.name : '')
            });

            const refreshFees = PaymentForm.bindFeeFilter({
                students: () => students,
                fees: () => fees
            });
            refreshFees();

            document.getElementById('paymentDate').value = new Date().toISOString().slice(0, 10);

            if (!students.length || !fees.length) {
                UI.toast('Add students and fee items before recording payments.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
