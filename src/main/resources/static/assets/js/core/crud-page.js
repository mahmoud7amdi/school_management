/**
 * Shared behaviour for the academic list pages (years, grades, subjects,
 * classrooms): fetch, render, client-side search, edit modal, delete.
 *
 * Those four pages differ only in their columns, form fields and endpoint, so
 * the loop lives here and each page supplies just the differences.
 *
 * Usage:
 *   CrudPage.create({
 *     endpoint: '/api/v1/grades',
 *     colspan: 4,
 *     tbody: document.getElementById('...'),
 *     searchInput: document.getElementById('...'),
 *     form: document.getElementById('...'),
 *     modal: document.getElementById('...'),
 *     emptyIcon: 'ti-stairs-up',
 *     emptyText: 'No grades yet.',
 *     label: 'grade',
 *     row: (item) => '<tr>...</tr>',
 *     searchFields: (item) => [item.name],
 *     fillForm: (item) => { ... },
 *     toPayload: () => ({ ... }),
 *     onLoaded: (items) => { ... }
 *   });
 */
(function (window) {
    'use strict';

    function create(config) {
        const state = {items: []};

        function render(list) {
            const summary = config.summary;
            if (summary) {
                summary.textContent = state.items.length + ' ' + config.label +
                    (state.items.length === 1 ? '' : 's');
            }
            if (!list.length) {
                UI.table.empty(
                    config.tbody,
                    config.colspan,
                    state.items.length
                        ? 'Nothing matches your search.'
                        : (config.emptyText || 'Nothing here yet.'),
                    config.emptyIcon);
                return;
            }
            config.tbody.innerHTML = list.map(config.row).join('');
        }

        async function load() {
            UI.table.loading(config.tbody, config.colspan, 'Loading ' + config.label + 's...');
            try {
                state.items = await Api.get(config.endpoint);
                render(state.items);
                if (config.onLoaded) {
                    config.onLoaded(state.items);
                }
            } catch (error) {
                UI.table.error(config.tbody, config.colspan,
                    error.message || 'Failed to load ' + config.label + 's.');
            }
        }

        function filter() {
            const term = config.searchInput.value.trim().toLowerCase();
            if (!term) {
                render(state.items);
                return;
            }
            render(state.items.filter(function (item) {
                return config.searchFields(item)
                    .some((field) => field && String(field).toLowerCase().includes(term));
            }));
        }

        function openEdit(id) {
            const item = state.items.find((entry) => String(entry.id) === String(id));
            if (!item) {
                return;
            }
            UI.clearErrors(config.form);
            state.editingId = item.id;
            config.fillForm(item);
            bootstrap.Modal.getOrCreateInstance(config.modal).show();
        }

        async function remove(id, name) {
            const confirmed = await UI.confirmDialog({
                title: 'Delete ' + config.label + '?',
                message: 'This removes ' + name + '. This cannot be undone.',
                okText: 'Delete'
            });
            if (!confirmed) {
                return;
            }
            try {
                await Api.del(config.endpoint + '/' + id);
                UI.toast(config.label.charAt(0).toUpperCase() + config.label.slice(1) + ' deleted', 'success');
                load();
            } catch (error) {
                // Most failures here are "still in use" guards from the service layer.
                UI.toast(error.message || 'Could not delete the ' + config.label, 'danger');
            }
        }

        // Only the two actions this module owns are handled. Matching on 'delete'
        // explicitly rather than treating everything-but-edit as a delete lets a page add
        // its own row actions without them opening a delete dialog.
        config.tbody.addEventListener('click', function (event) {
            const button = event.target.closest('[data-action]');
            if (!button) {
                return;
            }
            const action = button.getAttribute('data-action');
            const id = button.getAttribute('data-id');

            if (action === 'edit') {
                openEdit(id);
            } else if (action === 'delete') {
                remove(id, button.getAttribute('data-name') || 'this ' + config.label);
            }
        });

        if (config.searchInput) {
            config.searchInput.addEventListener('input', filter);
        }

        if (config.form) {
            UI.bindForm(config.form, {
                submitButton: config.submitButton,
                onSubmit: () => Api.put(config.endpoint + '/' + state.editingId, config.toPayload()),
                onSuccess: function () {
                    bootstrap.Modal.getInstance(config.modal).hide();
                    UI.toast('Changes saved', 'success');
                    load();
                }
            });
        }

        return {load: load, items: () => state.items};
    }

    window.CrudPage = {create: create};
})(window);
