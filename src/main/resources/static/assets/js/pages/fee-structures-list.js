/** Fee items list, with collection progress per item. */
(function () {
    'use strict';

    const TYPE_CLASSES = {
        TUITION: 'bg-primary-subtle text-primary-emphasis',
        TRANSPORT: 'bg-info-subtle text-info-emphasis',
        EXAM: 'bg-warning-subtle text-warning-emphasis',
        HOSTEL: 'bg-success-subtle text-success-emphasis'
    };

    function money(value) {
        if (value === null || value === undefined) {
            return '—';
        }
        return Number(value).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2});
    }

    function collected(fee) {
        const total = Number(fee.amount) || 0;
        const paid = Number(fee.totalCollected) || 0;
        if (!total) {
            return money(paid);
        }
        // Collection can exceed the per-student amount once several students pay,
        // so the bar is capped rather than scaled to the largest value.
        const pct = Math.min(Math.round((paid / total) * 100), 100);
        return '<div class="d-flex align-items-center gap-2">' +
            '<span class="mini-bar-track" style="width:70px">' +
            '<span class="mini-bar-fill" style="width:' + pct + '%"></span></span>' +
            '<span class="small">' + money(paid) + '</span>' +
            '</div>';
    }

    function dueCell(fee) {
        const label = UI.formatDate(fee.dueDate);
        const overdue = fee.dueDate && new Date(fee.dueDate) < new Date();
        return overdue
            ? '<span class="text-danger fw-semibold">' + UI.escapeHtml(label) + '</span>'
            : UI.dash(label);
    }

    function row(fee) {
        const name = UI.escapeHtml(fee.name);
        return '<tr>' +
            '<td>' +
                '<span class="fw-semibold">' + name + '</span>' +
                (fee.description
                    ? '<div class="small text-secondary">' + UI.escapeHtml(fee.description) + '</div>'
                    : '') +
            '</td>' +
            '<td>' + UI.badge(fee.feeTypeLabel || fee.feeType,
                TYPE_CLASSES[fee.feeType] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td>' + UI.dash(fee.grade ? fee.grade.name : null) + '</td>' +
            '<td>' + UI.dash(fee.academicYear ? fee.academicYear.name : null) + '</td>' +
            '<td class="fw-semibold">' + UI.escapeHtml(money(fee.amount)) + '</td>' +
            '<td>' + collected(fee) + '</td>' +
            '<td>' + dueCell(fee) + '</td>' +
            '<td class="school-col">' + UI.dash(fee.school ? fee.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + fee.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + fee.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/fees/structures',
        label: 'fee item',
        colspan: 9,
        emptyIcon: 'ti-cash-off',
        emptyText: 'No fee items yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (fee) => [
            fee.name,
            fee.description,
            fee.feeTypeLabel,
            fee.grade ? fee.grade.name : null,
            fee.academicYear ? fee.academicYear.name : null,
            fee.school ? fee.school.name : null
        ],
        fillForm: function (fee) {
            document.getElementById('name').value = fee.name || '';
            document.getElementById('feeType').value = fee.feeType || 'TUITION';
            document.getElementById('amount').value = fee.amount || '';
            document.getElementById('dueDate').value = fee.dueDate || '';
            document.getElementById('description').value = fee.description || '';
            document.getElementById('gradeId').value = fee.grade ? fee.grade.id : '';
            document.getElementById('academicYearId').value = fee.academicYear ? fee.academicYear.id : '';
        },
        toPayload: () => ({
            name: document.getElementById('name').value.trim(),
            feeType: document.getElementById('feeType').value,
            amount: document.getElementById('amount').value,
            dueDate: document.getElementById('dueDate').value,
            description: document.getElementById('description').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            academicYearId: document.getElementById('academicYearId').value
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [grades, years] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/academic-years')
            ]);

            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            UI.fillSelect(document.getElementById('academicYearId'), years, {
                placeholder: 'Select academic year',
                label: (year) => showSchool && year.school
                    ? year.name + ' — ' + year.school.name
                    : year.name
            });

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load fee items', 'danger');
            }
        });
})();
