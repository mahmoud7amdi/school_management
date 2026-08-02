/** Grades list. */
(function () {
    'use strict';

    function row(grade) {
        const name = UI.escapeHtml(grade.name);
        const level = grade.levelOrder
            ? '<span class="avatar-initial">' + UI.escapeHtml(grade.levelOrder) + '</span>'
            : '<span class="text-secondary">&mdash;</span>';

        return '<tr>' +
            '<td>' + level + '</td>' +
            '<td class="fw-semibold">' + name + '</td>' +
            '<td>' + UI.dash(grade.description) + '</td>' +
            '<td>' + UI.badge(grade.studentCount + ' student' + (grade.studentCount === 1 ? '' : 's'),
                grade.studentCount ? 'bg-info-subtle text-info-emphasis' : 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td class="school-col">' + UI.dash(grade.school ? grade.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + grade.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + grade.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/grades',
        label: 'grade',
        colspan: 6,
        emptyIcon: 'ti-stairs-up',
        emptyText: 'No grades yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (grade) => [grade.name, grade.description, grade.school ? grade.school.name : null],
        fillForm: function (grade) {
            document.getElementById('name').value = grade.name || '';
            document.getElementById('levelOrder').value = grade.levelOrder || '';
            document.getElementById('description').value = grade.description || '';
        },
        toPayload: () => ({
            name: document.getElementById('name').value.trim(),
            levelOrder: document.getElementById('levelOrder').value || null,
            description: document.getElementById('description').value.trim() || null
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(page.load)
        .catch(function () { /* redirect already under way */ });
})();
