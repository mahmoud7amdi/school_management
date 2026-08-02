/** Schools list: search, inline edit modal, delete. Super admin only. */
(function () {
    'use strict';

    const COLSPAN = 7;
    const tbody = document.getElementById('schoolsTableBody');
    const searchInput = document.getElementById('schoolSearch');
    const editForm = document.getElementById('editSchoolForm');
    const modalEl = document.getElementById('editSchoolModal');

    let schools = [];

    function websiteCell(url) {
        if (!url) {
            return '<span class="text-secondary">&mdash;</span>';
        }
        const safe = UI.escapeHtml(url);
        const href = /^https?:\/\//i.test(url) ? safe : 'https://' + safe;
        return '<a href="' + href + '" target="_blank" rel="noopener noreferrer" ' +
            'class="text-decoration-none">' + safe + ' <i class="ti ti-external-link"></i></a>';
    }

    function rowHtml(school) {
        const name = UI.escapeHtml(school.name);
        return '<tr>' +
            '<td>' +
                '<div class="d-flex align-items-center gap-2">' +
                    '<span class="avatar-initial">' + UI.escapeHtml(UI.initials(school.name)) + '</span>' +
                    '<div class="fw-semibold">' + name + '</div>' +
                '</div>' +
            '</td>' +
            '<td>' + UI.dash(school.email) + '</td>' +
            '<td>' + UI.dash(school.phoneNumber) + '</td>' +
            '<td>' + UI.dash(school.address) + '</td>' +
            '<td>' + websiteCell(school.website) + '</td>' +
            '<td>' + (school.active === false
                ? UI.badge('Inactive', 'bg-secondary-subtle text-secondary-emphasis')
                : UI.badge('Active', 'bg-success-subtle text-success-emphasis')) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + school.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + school.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    function render(list) {
        const summary = document.getElementById('resultSummary');
        if (summary) {
            summary.textContent = schools.length + ' school' + (schools.length === 1 ? '' : 's') + ' registered';
        }
        if (!list.length) {
            UI.table.empty(tbody, COLSPAN,
                schools.length ? 'No schools match your search.' : 'No schools yet.', 'ti-building-off');
            return;
        }
        tbody.innerHTML = list.map(rowHtml).join('');
    }

    async function load() {
        UI.table.loading(tbody, COLSPAN, 'Loading schools...');
        try {
            schools = await Api.get('/api/v1/schools');
            render(schools);
        } catch (error) {
            UI.table.error(tbody, COLSPAN, error.message || 'Failed to load schools.');
        }
    }

    // The whole list is in memory, so filtering client-side is instant here.
    function filter() {
        const term = searchInput.value.trim().toLowerCase();
        if (!term) {
            render(schools);
            return;
        }
        render(schools.filter(function (school) {
            return [school.name, school.email, school.phoneNumber, school.address, school.website]
                .some((field) => field && String(field).toLowerCase().includes(term));
        }));
    }

    function openEdit(id) {
        const school = schools.find((item) => String(item.id) === String(id));
        if (!school) {
            return;
        }
        UI.clearErrors(editForm);
        document.getElementById('editSchoolId').value = school.id;
        document.getElementById('name').value = school.name || '';
        document.getElementById('email').value = school.email || '';
        document.getElementById('phoneNumber').value = school.phoneNumber || '';
        document.getElementById('website').value = school.website || '';
        document.getElementById('address').value = school.address || '';
        document.getElementById('active').checked = school.active !== false;
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    async function remove(id, name) {
        const confirmed = await UI.confirmDialog({
            title: 'Delete school?',
            message: 'This deletes ' + name + ' and all of its academic records.',
            okText: 'Delete'
        });
        if (!confirmed) {
            return;
        }
        try {
            await Api.del('/api/v1/schools/' + id);
            UI.toast('School deleted', 'success');
            load();
        } catch (error) {
            UI.toast(error.message || 'Could not delete the school', 'danger');
        }
    }

    tbody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action]');
        if (!button) {
            return;
        }
        const id = button.getAttribute('data-id');
        if (button.getAttribute('data-action') === 'edit') {
            openEdit(id);
        } else {
            remove(id, button.getAttribute('data-name') || 'this school');
        }
    });

    searchInput.addEventListener('input', filter);

    UI.bindForm(editForm, {
        submitButton: document.getElementById('updateSchoolBtn'),
        onSubmit: function () {
            const id = document.getElementById('editSchoolId').value;
            return Api.put('/api/v1/schools/' + id, {
                name: document.getElementById('name').value.trim(),
                email: document.getElementById('email').value.trim() || null,
                phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
                website: document.getElementById('website').value.trim() || null,
                address: document.getElementById('address').value.trim() || null,
                active: document.getElementById('active').checked
            });
        },
        onSuccess: function () {
            bootstrap.Modal.getInstance(modalEl).hide();
            UI.toast('School updated', 'success');
            load();
        }
    });

    Shell.requireRole('SUPER_ADMIN')
        .then(load)
        .catch(function () { /* redirect already under way */ });
})();
