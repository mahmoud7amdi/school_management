/**
 * Self-service profile.
 *
 * Posts to /auth/me, which is open to every role — the old page used the admin
 * user endpoint, so teachers could not edit themselves and even a super admin
 * was rejected by the role rules.
 */
(function () {
    'use strict';

    const form = document.getElementById('profileForm');

    function value(id) {
        const raw = document.getElementById(id).value.trim();
        return raw === '' ? null : raw;
    }

    function fill(user) {
        document.getElementById('fullName').value = user.fullName || '';
        document.getElementById('username').value = user.username || '';
        document.getElementById('email').value = user.email || '';
        document.getElementById('phoneNumber').value = user.phoneNumber || '';

        const since = document.getElementById('memberSince');
        if (since) {
            since.textContent = UI.formatDate(user.createdAt) || '—';
        }
    }

    UI.bindForm(form, {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: function () {
            const payload = {
                fullName: value('fullName'),
                username: value('username'),
                email: value('email'),
                phoneNumber: value('phoneNumber')
            };
            // The backend requires the current password before it will set a new one.
            const newPassword = document.getElementById('newPassword').value;
            if (newPassword) {
                payload.newPassword = newPassword;
                payload.currentPassword = document.getElementById('currentPassword').value;
            }
            return Api.put('/api/v1/auth/me', payload);
        },
        onSuccess: function (user) {
            UI.toast('Profile updated', 'success');
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';

            // Refresh the cached identity so the topbar reflects the new name.
            localStorage.setItem('sm.user', JSON.stringify(user));
            document.querySelectorAll('.user-full-name').forEach(function (el) {
                el.textContent = user.fullName || user.username;
            });
            document.querySelectorAll('.user-username').forEach(function (el) {
                el.textContent = '@' + user.username;
            });
        }
    });

    Shell.ready.then(function (user) {
        if (user) {
            fill(user);
        }
    });
})();
