// Schools page logic — handles both list and add forms.
$(function () {
    App.attachAuthHeader();

    const currentUserRole = localStorage.getItem('userRole');
    if (currentUserRole !== 'SUPER_ADMIN') {
        window.location.href = '/dashboard/index';
        return;
    }

    const onListPage = $('#schoolsTableBody').length > 0;
    const onAddPage  = $('#addSchoolForm').length > 0;
    if (onListPage) initListPage();
    if (onAddPage)  initAddPage();

    function cellOrDash(value) {
        if (value === null || value === undefined || value === '') {
            return '<span class="text-muted">—</span>';
        }
        return App.escapeHtml(value);
    }

    function websiteCell(url) {
        if (!url) return '<span class="text-muted">—</span>';
        const safe = App.escapeHtml(url);
        const href = /^https?:\/\//i.test(url) ? safe : 'https://' + safe;
        return '<a href="' + href + '" target="_blank" rel="noopener noreferrer" class="text-decoration-none">' + safe + ' <i class="ti ti-external-link"></i></a>';
    }

    function initListPage() {
        loadSchools();

        $('#schoolSearch').on('input', function () {
            const q = $(this).val().toLowerCase();
            $('#schoolsTableBody tr[data-row]').each(function () {
                $(this).toggle($(this).text().toLowerCase().indexOf(q) !== -1);
            });
        });

        $(document).on('click', '.edit-school-btn', function () {
            const id = $(this).data('id');
            $.ajax({
                url: '/api/v1/schools/' + id,
                type: 'GET',
                success: function (res) {
                    if (!res.data) return;
                    $('#editSchoolId').val(res.data.id);
                    $('#editName').val(res.data.name);
                    $('#editEmail').val(res.data.email);
                    $('#editPhone').val(res.data.phoneNumber);
                    $('#editAddress').val(res.data.address);
                    $('#editWebsite').val(res.data.website);
                    $('#editAlertContainer').empty();
                    $('#editSchoolForm').removeClass('was-validated');
                    bootstrap.Modal.getOrCreateInstance(document.getElementById('editSchoolModal')).show();
                }
            });
        });

        $('#editSchoolForm').on('submit', function (e) {
            e.preventDefault();
            const form = this;
            if (!form.checkValidity()) {
                e.stopPropagation();
                $(form).addClass('was-validated');
                return;
            }
            $('#updateSchoolBtn').prop('disabled', true);
            const id = $('#editSchoolId').val();
            const payload = {
                name:        $('#editName').val(),
                email:       $('#editEmail').val(),
                phoneNumber: $('#editPhone').val(),
                address:     $('#editAddress').val(),
                website:     $('#editWebsite').val()
            };
            $.ajax({
                url: '/api/v1/schools/' + id,
                type: 'PUT',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: function () {
                    bootstrap.Modal.getInstance(document.getElementById('editSchoolModal')).hide();
                    loadSchools();
                    $('#updateSchoolBtn').prop('disabled', false);
                },
                error: function (xhr) {
                    const msg = xhr.responseJSON ? xhr.responseJSON.message : 'Error updating school';
                    App.showAlert('#editAlertContainer', msg, 'danger');
                    $('#updateSchoolBtn').prop('disabled', false);
                }
            });
        });

        $(document).on('click', '.delete-school-btn', async function () {
            const id = $(this).data('id');
            const name = $(this).data('name') || 'this school';
            const ok = await App.confirmDialog({
                title: 'Delete school?',
                message: 'You are about to delete ' + name + '. This cannot be undone.',
                okText: 'Delete'
            });
            if (!ok) return;
            $.ajax({
                url: '/api/v1/schools/' + id,
                type: 'DELETE',
                success: loadSchools,
                error: function (xhr) {
                    alert(xhr.responseJSON ? xhr.responseJSON.message : 'Error deleting school');
                }
            });
        });
    }

    function loadSchools() {
        const $tbody = $('#schoolsTableBody');
        $tbody.html('<tr><td colspan="7" class="table-state"><i class="ti ti-loader-2"></i> Loading schools...</td></tr>');
        $.ajax({
            url: '/api/v1/schools',
            type: 'GET',
            success: function (res) {
                $tbody.empty();
                if (!res.data || res.data.length === 0) {
                    $tbody.html('<tr><td colspan="7" class="table-state"><i class="ti ti-building-off"></i> No schools yet.</td></tr>');
                    return;
                }
                res.data.forEach(function (s) {
                    const safeName = App.escapeHtml(s.name);
                    $tbody.append(
                        '<tr data-row>' +
                            '<td class="text-secondary">#' + s.id + '</td>' +
                            '<td>' +
                                '<div class="d-flex align-items-center gap-2">' +
                                    '<span class="avatar-initial">' + App.escapeHtml(App.initials(s.name)) + '</span>' +
                                    '<div class="fw-semibold">' + safeName + '</div>' +
                                '</div>' +
                            '</td>' +
                            '<td>' + cellOrDash(s.email) + '</td>' +
                            '<td>' + cellOrDash(s.phoneNumber) + '</td>' +
                            '<td>' + cellOrDash(s.address) + '</td>' +
                            '<td>' + websiteCell(s.website) + '</td>' +
                            '<td class="text-end row-actions">' +
                                '<button class="btn btn-sm btn-ghost-primary edit-school-btn" data-id="' + s.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                                '<button class="btn btn-sm btn-ghost-danger delete-school-btn" data-id="' + s.id + '" data-name="' + safeName + '" title="Delete"><i class="ti ti-trash fs-5"></i></button>' +
                            '</td>' +
                        '</tr>'
                    );
                });
            },
            error: function () {
                $tbody.html('<tr><td colspan="7" class="table-state text-danger"><i class="ti ti-alert-circle"></i> Failed to load schools.</td></tr>');
            }
        });
    }

    function initAddPage() {
        $('#addSchoolForm').on('submit', function (e) {
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
                name:        $('#name').val(),
                address:     $('#address').val(),
                phoneNumber: $('#phoneNumber').val(),
                email:       $('#email').val(),
                website:     $('#website').val()
            };
            $.ajax({
                url: '/api/v1/schools',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(payload),
                success: function () {
                    App.showAlert('#alertContainer', 'School added successfully!', 'success');
                    setTimeout(function () { window.location.href = '/dashboard/schools/all'; }, 1200);
                },
                error: function (xhr) {
                    const msg = xhr.responseJSON ? xhr.responseJSON.message : 'Error adding school';
                    App.showAlert('#alertContainer', msg, 'danger');
                    $('#saveBtn').prop('disabled', false);
                }
            });
        });
    }
});
