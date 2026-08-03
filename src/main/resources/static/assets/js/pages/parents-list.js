/**
 * Parents list.
 *
 * Editing opens the form page rather than a modal: the child links are the substance of a
 * parent record, and they need more room than a modal gives.
 */
(function () {
    'use strict';

    const COLSPAN = 6;

    const tbody = document.getElementById('tableBody');
    const summary = document.getElementById('resultSummary');
    const searchInput = document.getElementById('searchInput');

    let parents = [];

    function childCell(parent) {
        const children = parent.children || [];
        if (!children.length) {
            return '<span class="text-secondary">&mdash;</span>';
        }
        // Keep the cell short: name the first two and count the rest.
        const shown = children.slice(0, 2).map(function (child) {
            const label = child.studentName +
                (child.primaryContact ? ' ★' : '') +
                ' · ' + (child.relationshipLabel || '');
            return UI.badge(label, 'bg-primary-subtle text-primary-emphasis');
        }).join(' ');
        const extra = children.length - 2;
        return shown + (extra > 0 ? ' <span class="small text-secondary">+' + extra + '</span>' : '');
    }

    function contact(parent) {
        const parts = [];
        if (parent.email) {
            parts.push('<div class="small">' + UI.escapeHtml(parent.email) + '</div>');
        }
        if (parent.phoneNumber) {
            parts.push('<div class="small text-secondary">' + UI.escapeHtml(parent.phoneNumber) + '</div>');
        }
        return parts.length ? parts.join('') : '<span class="text-secondary">&mdash;</span>';
    }

    function rowHtml(parent) {
        const name = UI.escapeHtml(parent.fullName);
        const accountTag = parent.hasUserAccount
            ? ' <i class="ti ti-user-check text-success" title="Has a login"></i>'
            : '';

        return '<tr>' +
            '<td>' +
            '<div class="d-flex align-items-center gap-2">' +
            '<span class="avatar avatar-sm avatar-initial rounded-circle bg-primary-subtle text-primary-emphasis">' +
            UI.escapeHtml(UI.initials(parent.fullName)) + '</span>' +
            '<div><span class="fw-semibold">' + name + '</span>' + accountTag + '</div>' +
            '</div>' +
            '</td>' +
            '<td>' + contact(parent) + '</td>' +
            '<td>' + UI.dash(parent.occupation) + '</td>' +
            '<td>' + childCell(parent) + '</td>' +
            '<td class="school-col">' + UI.dash(parent.school ? parent.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
            '<a class="btn btn-sm btn-ghost-primary" title="Edit" ' +
            'href="/dashboard/parents/add?id=' + parent.id + '"><i class="ti ti-pencil fs-5"></i></a>' +
            '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
            'data-id="' + parent.id + '" data-name="' + name + '" title="Delete">' +
            '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
            '</tr>';
    }

    function matches(parent, term) {
        return [
            parent.fullName,
            parent.email,
            parent.phoneNumber,
            parent.occupation,
            parent.school ? parent.school.name : null
        ].concat((parent.children || []).map((child) => child.studentName))
            .some((field) => field && String(field).toLowerCase().includes(term));
    }

    function render() {
        const term = (searchInput.value || '').trim().toLowerCase();
        const visible = term ? parents.filter((parent) => matches(parent, term)) : parents;

        if (!parents.length) {
            summary.textContent = 'No parents yet.';
            UI.table.empty(tbody, COLSPAN, 'No parents yet.', 'ti-users');
            return;
        }
        if (!visible.length) {
            summary.textContent = 'No parents match "' + term + '".';
            UI.table.empty(tbody, COLSPAN, 'No parents match your search.', 'ti-search-off');
            return;
        }

        summary.textContent = visible.length === parents.length
            ? parents.length + ' parent' + (parents.length === 1 ? '' : 's')
            : visible.length + ' of ' + parents.length + ' parents';
        tbody.innerHTML = visible.map(rowHtml).join('');
    }

    function load() {
        UI.table.loading(tbody, COLSPAN, 'Loading parents...');
        return Api.get('/api/v1/parents')
            .then(function (result) {
                parents = result || [];
                render();
                if (!Shell.isSuperAdmin()) {
                    document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
                }
            })
            .catch(function (error) {
                summary.textContent = 'Could not load parents.';
                UI.table.error(tbody, COLSPAN, error.message || 'Failed to load parents.');
            });
    }

    searchInput.addEventListener('input', render);

    // Delegated: rows are replaced wholesale on every render.
    tbody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action="delete"]');
        if (!button) {
            return;
        }
        const id = button.getAttribute('data-id');
        const name = button.getAttribute('data-name');

        UI.confirmDialog({
            title: 'Delete parent?',
            message: 'This removes ' + name + ' and their links to any children. '
                + 'It does not affect the students themselves.',
            okText: 'Delete'
        }).then(function (confirmed) {
            if (!confirmed) {
                return;
            }
            Api.del('/api/v1/parents/' + id)
                .then(function () {
                    UI.toast('Parent deleted', 'success');
                    return load();
                })
                .catch(function (error) {
                    UI.toast(error.message || 'Could not delete that parent', 'danger');
                });
        });
    });

    Shell.requireManager()
        .then(load)
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load parents', 'danger');
            }
        });
})();
