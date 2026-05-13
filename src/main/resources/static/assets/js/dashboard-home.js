// Dashboard home: load counts and role badge.
$(function () {
    App.attachAuthHeader();

    const role = localStorage.getItem('userRole');
    $('#statRole').text(role ? role.replace(/_/g, ' ') : '—');

    // Schools section is hidden for SCHOOL_ADMIN.
    if (role === 'SCHOOL_ADMIN') {
        $('.nav-schools-item').hide();
    } else if (role === 'SUPER_ADMIN') {
        $.ajax({
            url: '/api/v1/schools',
            type: 'GET',
            success: function (res) {
                const count = (res && res.data) ? res.data.length : 0;
                $('#statSchoolsCount').text(count);
            },
            error: function () { $('#statSchoolsCount').text('—'); }
        });
    }

    $.ajax({
        url: '/api/v1/users',
        type: 'GET',
        success: function (res) {
            const count = (res && res.data) ? res.data.length : 0;
            $('#statUsersCount').text(count);
        },
        error: function () { $('#statUsersCount').text('—'); }
    });
});
