/** Users list: search, edit modal, delete. Role options depend on who is signed in. */
(function () {
    'use strict';

    const COLSPAN = 6;
    const tbody = document.getElementById('usersTableBody');
    const searchInput = document.getElementById('userSearch');
    const editForm = document.getElementById('editUserForm');
    const modalEl = document.getElementById('editUserModal');

    let users = [];

    const ROLE_CLASSES = {
        SUPER_ADMIN: 'bg-danger-subtle text-danger-emphasis',
        SCHOOL_ADMIN: 'bg-primary-subtle text-primary-emphasis',
        TEACHER: 'bg-info-subtle text-info-emphasis',
        STUDENT: 'bg-success-subtle text-success-emphasis',
        PARENT: 'bg-warning-subtle text-warning-emphasis'
    };

    /** A super admin only ever manages school admins; a school admin manages staff. */
    function assignableRoles() {
        return Shell.isSuperAdmin()
            ? [{id: 'SCHOOL_ADMIN', name: 'School Admin'}]
            : [
                {id: 'TEACHER', name: 'Teacher'},
                {id: 'STUDENT', name: 'Student'},
                {id: 'PARENT', name: 'Parent'}
            ];
    }

    function rowHtml(user) {
        const name = UI.escapeHtml(user.fullName || user.username);
        return '<tr>' +
            '<td>' +
                '<div class="d-flex align-items-center gap-2">' +
                    '<span class="avatar-initial">' +
                        UI.escapeHtml(UI.initials(user.fullName || user.username)) + '</span>' +
                    '<div>' +
                        '<div class="fw-semibold">' + name + '</div>' +
                        '<div class="small text-secondary">@' + UI.escapeHtml(user.username) + '</div>' +
                    '</div>' +
                '</div>' +
            '</td>' +
            '<td>' + UI.dash(user.email) + '</td>' +
            '<td>' + UI.badge(user.roleLabel || user.role, ROLE_CLASSES[user.role]) + '</td>' +
            '<td class="school-col">' + UI.dash(user.school ? user.school.name : null) + '</td>' +
            '<td>' + (user.active
                ? UI.badge('Active', 'bg-success-subtle text-success-emphasis')
                : UI.badge('Inactive', 'bg-secondary-subtle text-secondary-emphasis')) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + user.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + user.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    function render(list) {
        const summary = document.getElementById('resultSummary');
        if (summary) {
            summary.textContent = users.length + ' user' + (users.length === 1 ? '' : 's');
        }
        if (!list.length) {
            UI.table.empty(tbody, COLSPAN,
                users.length ? 'No users match your search.' : 'No users yet.', 'ti-users-off');
            return;
        }
        tbody.innerHTML = list.map(rowHtml).join('');
        // A school admin's users all share one school, so the column adds nothing.
        if (!Shell.isSuperAdmin()) {
            document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
        }
    }

    async function load() {
        UI.table.loading(tbody, COLSPAN, 'Loading users...');
        try {
            users = await Api.get('/api/v1/users');
            render(users);
        } catch (error) {
            UI.table.error(tbody, COLSPAN, error.message || 'Failed to load users.');
        }
    }

    function filter() {
        const term = searchInput.value.trim().toLowerCase();
        if (!term) {
            render(users);
            return;
        }
        render(users.filter(function (user) {
            return [user.fullName, user.username, user.email]
                .some((field) => field && String(field).toLowerCase().includes(term));
        }));
    }

    function openEdit(id) {
        const user = users.find((item) => String(item.id) === String(id));
        if (!user) {
            return;
        }
        UI.clearErrors(editForm);
        document.getElementById('editUserId').value = user.id;
        document.getElementById('fullName').value = user.fullName || '';
        document.getElementById('username').value = user.username || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phoneNumber').value = user.phoneNumber || '';
        document.getElementById('password').value = '';
        document.getElementById('active').checked = !!user.active;

        UI.fillSelect(document.getElementById('role'), assignableRoles(), {placeholder: null});
        document.getElementById('role').value = user.role;

        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }

    async function remove(id, name) {
        const confirmed = await UI.confirmDialog({
            title: 'Delete user?',
            message: 'This permanently removes the account for ' + name + '.',
            okText: 'Delete'
        });
        if (!confirmed) {
            return;
        }
        try {
            await Api.del('/api/v1/users/' + id);
            UI.toast('User deleted', 'success');
            load();
        } catch (error) {
            UI.toast(error.message || 'Could not delete the user', 'danger');
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
            remove(id, button.getAttribute('data-name') || 'this user');
        }
    });

    searchInput.addEventListener('input', filter);

    UI.bindForm(editForm, {
        submitButton: document.getElementById('updateBtn'),
        onSubmit: function () {
            const id = document.getElementById('editUserId').value;
            const payload = {
                fullName: document.getElementById('fullName').value.trim(),
                username: document.getElementById('username').value.trim(),
                email: document.getElementById('email').value.trim(),
                phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
                role: document.getElementById('role').value,
                active: document.getElementById('active').checked
            };
            // Only send a password when one was typed, else it would be re-hashed blank.
            const password = document.getElementById('password').value;
            if (password) {
                payload.password = password;
            }
            return Api.put('/api/v1/users/' + id, payload);
        },
        onSuccess: function () {
            bootstrap.Modal.getInstance(modalEl).hide();
            UI.toast('User updated', 'success');
            load();
        }
    });

    Shell.requireManager()
        .then(load)
        .catch(function () { /* redirect already under way */ });
})();
