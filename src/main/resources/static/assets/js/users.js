// Users page logic — handles both list and add forms.
$(function () {
    App.attachAuthHeader();

    const currentUserRole = localStorage.getItem('userRole');
    if (currentUserRole !== 'SUPER_ADMIN' && currentUserRole !== 'SCHOOL_ADMIN') {
        window.location.href = '/dashboard/index';
        return;
    }
    if (currentUserRole === 'SCHOOL_ADMIN') {
        $('.nav-schools-item').hide();
    }

    // Page detection via element presence.
    const onListPage = $('#usersTableBody').length > 0;
    const onAddPage  = $('#addUserForm').length > 0;
    if (onListPage) initListPage();
    if (onAddPage)  initAddPage();

    // -------- helpers ------------------------------------------------------
    function roleBadge(role) {
        const map = {
            SUPER_ADMIN:  'bg-danger-subtle text-danger-emphasis',
            SCHOOL_ADMIN: 'bg-primary-subtle text-primary-emphasis',
            TEACHER:      'bg-info-subtle text-info-emphasis',
            STUDENT:      'bg-success-subtle text-success-emphasis',
            PARENT:       'bg-warning-subtle text-warning-emphasis'
        };
        const cls = map[role] || 'bg-secondary-subtle text-secondary-emphasis';
        return '<span class="badge ' + cls + '">' + App.escapeHtml((role || '').replace(/_/g, ' ')) + '</span>';
    }
    function statusBadge(active) {
        return active
            ? '<span class="badge bg-success-subtle text-success-emphasis"><i class="ti ti-circle-check me-1"></i>Active</span>'
            : '<span class="badge bg-danger-subtle text-danger-emphasis"><i class="ti ti-circle-x me-1"></i>Inactive</span>';
    }

    // -------- list page ----------------------------------------------------
    function initListPage() {
        if (currentUserRole === 'SUPER_ADMIN') {
            $('#editRole').append('<option value="SCHOOL_ADMIN">School Admin</option>');
        } else if (currentUserRole === 'SCHOOL_ADMIN') {
            $('#editRole').append('<option value="TEACHER">Teacher</option><option value="STUDENT">Student</option><option value="PARENT">Parent</option>');
            $('.school-col').hide();
        }

        loadUsers();

        // Client-side filter
        $('#userSearch').on('input', function () {
            const q = $(this).val().toLowerCase();
            $('#usersTableBody tr[data-row]').each(function () {
                $(this).toggle($(this).text().toLowerCase().indexOf(q) !== -1);
            });
        });

        $(document).on('click', '.edit-user-btn', function () {
            const id = $(this).data('id');
            $.ajax({
                url: '/api/v1/users/profile/id/' + id,
                type: 'GET',
                success: function (res) {
                    if (!res.data) return;
                    $('#editUserId').val(res.data.id);
                    $('#editUsername').val(res.data.username);
                    $('#editEmail').val(res.data.email);
                    $('#editRole').val(res.data.role);
                    $('#editActive').prop('checked', res.data.active);
                    $('#editPassword').val('');
                    $('#editAlertContainer').empty();
                    bootstrap.Modal.getOrCreateInstance(document.getElementById('editUserModal')).show();
                }
            });
        });

        $('#editUserForm').on('submit', function (e) {
            e.preventDefault();
            $('#updateBtn').prop('disabled', true);
            const id = $('#editUserId').val();
            const payload = {
                username: $('#editUsername').val(),
                email:    $('#editEmail').val(),
                role:     $('#editRole').val(),
                active:   $('#editActive').is(':checked')
            };
            const pw = $('#editPassword').val();
            if (pw) payload.password = pw;

            $.ajax({
                url: '/api/v1/users/' + id,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: function () {
                    bootstrap.Modal.getInstance(document.getElementById('editUserModal')).hide();
                    loadUsers();
                    $('#updateBtn').prop('disabled', false);
                },
                error: function (xhr) {
                    const msg = xhr.responseJSON ? xhr.responseJSON.message : 'Error updating user';
                    App.showAlert('#editAlertContainer', msg, 'danger');
                    $('#updateBtn').prop('disabled', false);
                }
            });
        });

        $(document).on('click', '.delete-user-btn', async function () {
            const id = $(this).data('id');
            const username = $(this).data('username') || 'this user';
            const ok = await App.confirmDialog({
                title: 'Delete user?',
                message: 'You are about to delete ' + username + '. This cannot be undone.',
                okText: 'Delete'
            });
            if (!ok) return;
            $.ajax({
                url: '/api/v1/users/' + id,
                type: 'DELETE',
                success: loadUsers,
                error: function (xhr) {
                    alert(xhr.responseJSON ? xhr.responseJSON.message : 'Error deleting user');
                }
            });
        });
    }

    function loadUsers() {
        const $tbody = $('#usersTableBody');
        $tbody.html('<tr><td colspan="7" class="table-state"><i class="ti ti-loader-2"></i> Loading users...</td></tr>');
        $.ajax({
            url: '/api/v1/users',
            type: 'GET',
            success: function (res) {
                $tbody.empty();
                if (!res.data || res.data.length === 0) {
                    $tbody.html('<tr><td colspan="7" class="table-state"><i class="ti ti-users-off"></i> No users yet.</td></tr>');
                    return;
                }
                res.data.forEach(function (u) {
                    const name = App.escapeHtml(u.username);
                    const email = App.escapeHtml(u.email);
                    const schoolName = u.school ? App.escapeHtml(u.school.name) : '<span class="text-muted">N/A</span>';
                    $tbody.append(
                        '<tr data-row>' +
                            '<td class="text-secondary">#' + u.id + '</td>' +
                            '<td>' +
                                '<div class="d-flex align-items-center gap-2">' +
                                    '<span class="avatar-initial">' + App.escapeHtml(App.initials(u.username)) + '</span>' +
                                    '<div><div class="fw-semibold">' + name + '</div></div>' +
                                '</div>' +
                            '</td>' +
                            '<td>' + email + '</td>' +
                            '<td>' + roleBadge(u.role) + '</td>' +
                            '<td>' + statusBadge(u.active) + '</td>' +
                            '<td class="school-col">' + schoolName + '</td>' +
                            '<td class="text-end row-actions">' +
                                '<button class="btn btn-sm btn-ghost-primary edit-user-btn" data-id="' + u.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                                '<button class="btn btn-sm btn-ghost-danger delete-user-btn" data-id="' + u.id + '" data-username="' + name + '" title="Delete"><i class="ti ti-trash fs-5"></i></button>' +
                            '</td>' +
                        '</tr>'
                    );
                });
                if (currentUserRole === 'SCHOOL_ADMIN') $('.school-col').hide();
            },
            error: function () {
                $tbody.html('<tr><td colspan="7" class="table-state text-danger"><i class="ti ti-alert-circle"></i> Failed to load users.</td></tr>');
            }
        });
    }

    // -------- add page -----------------------------------------------------
    function initAddPage() {
        if (currentUserRole === 'SUPER_ADMIN') {
            $('#role').append('<option value="SCHOOL_ADMIN">School Admin</option>');
            $('#schoolSelectContainer').show();
            $('#schoolId').prop('required', true);
            $.ajax({
                url: '/api/v1/schools',
                type: 'GET',
                success: function (res) {
                    if (!res.data) return;
                    res.data.forEach(function (s) {
                        $('#schoolId').append('<option value="' + s.id + '">' + App.escapeHtml(s.name) + '</option>');
                    });
                }
            });
        } else {
            $('#role').append('<option value="TEACHER">Teacher</option><option value="STUDENT">Student</option><option value="PARENT">Parent</option>');
        }

        $('#addUserForm').on('submit', function (e) {
            e.preventDefault();
            const form = this;
            if (!form.checkValidity()) {
                e.stopPropagation();
                $(form).addClass('was-validated');
                return;
            }

            $('#saveBtn').prop('disabled', true);
            $('#alertContainer').empty();

            const payload = {
                username: $('#username').val(),
                email:    $('#email').val(),
                password: $('#password').val(),
                role:     $('#role').val()
            };
            if ($('#schoolId').is(':visible') && $('#schoolId').val()) {
                payload.school = { id: $('#schoolId').val() };
            }

            $.ajax({
                url: '/api/v1/users/create',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: function () {
                    App.showAlert('#alertContainer', 'User created successfully!', 'success');
                    setTimeout(function () { window.location.href = '/dashboard/users/all'; }, 1200);
                },
                error: function (xhr) {
                    const msg = xhr.responseJSON ? xhr.responseJSON.message : 'Error creating user';
                    App.showAlert('#alertContainer', msg, 'danger');
                    $('#saveBtn').prop('disabled', false);
                }
            });
        });
    }
});
