/**
 * Shared wiring for the two payment forms.
 *
 * The service rejects a payment whose fee item does not apply to the student's
 * grade, so the picker is narrowed by the selected student rather than offering
 * every fee item in the school.
 */
(function (window) {
    'use strict';

    function money(value) {
        if (value === null || value === undefined) {
            return '—';
        }
        return Number(value).toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function bindFeeFilter(config) {
        const studentSelect = document.getElementById(config.studentId || 'studentId');
        const feeSelect = document.getElementById(config.feeId || 'feeStructureId');

        function refresh(selectedId) {
            const studentValue = studentSelect.value;
            const student = (config.students() || []).find(function (candidate) {
                return String(candidate.id) === String(studentValue);
            });
            const gradeId = student && student.grade ? student.grade.id : null;

            const matching = (config.fees() || []).filter(function (fee) {
                return gradeId && fee.grade && String(fee.grade.id) === String(gradeId);
            });

            UI.fillSelect(feeSelect, matching, {
                placeholder: gradeId ? 'Select fee item' : 'Choose a student first',
                label: (fee) => fee.name + ' — ' + money(fee.amount) +
                    (fee.academicYear ? ' (' + fee.academicYear.name + ')' : '')
            });
            if (selectedId !== undefined) {
                feeSelect.value = selectedId === null ? '' : String(selectedId);
            }
        }

        studentSelect.addEventListener('change', () => refresh());
        return refresh;
    }

    window.PaymentForm = {bindFeeFilter: bindFeeFilter, money: money};
})(window);
