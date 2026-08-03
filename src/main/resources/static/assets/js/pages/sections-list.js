/** Sections list. The edit modal needs grades and teachers, so it loads those first. */
(function () {
    'use strict';

    function row(section) {
        const name = UI.escapeHtml(section.name);
        return '<tr>' +
            '<td class="fw-semibold">' + name + '</td>' +
            '<td>' + UI.dash(section.grade ? section.grade.name : null) + '</td>' +
            '<td>' + UI.dash(section.sectionHead ? section.sectionHead.name : null) + '</td>' +
            '<td>' + UI.dash(section.capacity) + '</td>' +
            '<td>' + UI.badge(section.classroomCount + (section.classroomCount === 1 ? ' class' : ' classes'),
                'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td class="school-col">' + UI.dash(section.school ? section.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + section.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + section.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/sections',
        label: 'section',
        colspan: 7,
        emptyIcon: 'ti-layout-grid',
        emptyText: 'No sections yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (section) => [
            section.name,
            section.description,
            section.grade ? section.grade.name : null,
            section.sectionHead ? section.sectionHead.name : null,
            section.school ? section.school.name : null
        ],
        fillForm: function (section) {
            document.getElementById('name').value = section.name || '';
            document.getElementById('capacity').value = section.capacity || '';
            document.getElementById('description').value = section.description || '';
            document.getElementById('gradeId').value = section.grade ? section.grade.id : '';
            document.getElementById('sectionHeadId').value = section.sectionHead ? section.sectionHead.id : '';
        },
        toPayload: () => ({
            name: document.getElementById('name').value.trim(),
            capacity: document.getElementById('capacity').value || null,
            description: document.getElementById('description').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            sectionHeadId: document.getElementById('sectionHeadId').value || null
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
            const [grades, teachers] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/teachers')
            ]);

            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            UI.fillSelect(document.getElementById('sectionHeadId'), teachers, {
                placeholder: 'No section head',
                label: (teacher) => teacher.fullName
            });

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load sections', 'danger');
            }
        });
})();
