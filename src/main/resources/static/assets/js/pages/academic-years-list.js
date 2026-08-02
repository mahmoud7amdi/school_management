/** Academic years list. */
(function () {
    'use strict';

    function row(year) {
        const name = UI.escapeHtml(year.name);
        const currentTag = year.current
            ? ' ' + UI.badge('Current', 'bg-success-subtle text-success-emphasis')
            : '';
        return '<tr>' +
            '<td><span class="fw-semibold">' + name + '</span>' + currentTag + '</td>' +
            '<td>' + UI.dash(UI.formatDate(year.startDate)) + '</td>' +
            '<td>' + UI.dash(UI.formatDate(year.endDate)) + '</td>' +
            '<td class="school-col">' + UI.dash(year.school ? year.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + year.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + year.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/academic-years',
        label: 'academic year',
        colspan: 5,
        emptyIcon: 'ti-calendar-off',
        emptyText: 'No academic years yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (year) => [year.name, year.school ? year.school.name : null],
        fillForm: function (year) {
            document.getElementById('name').value = year.name || '';
            document.getElementById('startDate').value = year.startDate || '';
            document.getElementById('endDate').value = year.endDate || '';
            document.getElementById('current').checked = !!year.current;
        },
        toPayload: () => ({
            name: document.getElementById('name').value.trim(),
            startDate: document.getElementById('startDate').value,
            endDate: document.getElementById('endDate').value,
            current: document.getElementById('current').checked
        }),
        onLoaded: function () {
            // One school for a school admin, so the column is noise.
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(page.load)
        .catch(function () { /* redirect already under way */ });
})();
