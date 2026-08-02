/** Subjects list. The edit modal needs the grade list, so it loads that first. */
(function () {
    'use strict';

    function row(subject) {
        const name = UI.escapeHtml(subject.name);
        return '<tr>' +
            '<td class="fw-semibold">' + name + '</td>' +
            '<td>' + (subject.code
                ? UI.badge(subject.code, 'bg-secondary-subtle text-secondary-emphasis')
                : '<span class="text-secondary">&mdash;</span>') + '</td>' +
            '<td>' + UI.dash(subject.grade ? subject.grade.name : null) + '</td>' +
            '<td>' + UI.dash(subject.weeklyHours ? subject.weeklyHours + ' h' : null) + '</td>' +
            '<td class="school-col">' + UI.dash(subject.school ? subject.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + subject.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + subject.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/subjects',
        label: 'subject',
        colspan: 6,
        emptyIcon: 'ti-book-off',
        emptyText: 'No subjects yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (subject) => [
            subject.name,
            subject.code,
            subject.grade ? subject.grade.name : null,
            subject.school ? subject.school.name : null
        ],
        fillForm: function (subject) {
            document.getElementById('name').value = subject.name || '';
            document.getElementById('code').value = subject.code || '';
            document.getElementById('weeklyHours').value = subject.weeklyHours || '';
            document.getElementById('gradeId').value = subject.grade ? subject.grade.id : '';
        },
        toPayload: () => ({
            name: document.getElementById('name').value.trim(),
            code: document.getElementById('code').value.trim() || null,
            weeklyHours: document.getElementById('weeklyHours').value || null,
            gradeId: document.getElementById('gradeId').value
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(async function () {
            // Populate the modal's grade picker before any row can open it.
            const grades = await Api.get('/api/v1/grades');
            const showSchool = Shell.isSuperAdmin();
            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load subjects', 'danger');
            }
        });
})();
