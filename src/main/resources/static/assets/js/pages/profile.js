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
    const FALLBACK_AVATAR = '/assets/images/avatar/avatar-fallback.jpg';
    const MAX_AVATAR_BYTES = 2 * 1024 * 1024;

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
        showAvatar(user.avatarUrl);
    }

    // --- profile picture -----------------------------------------------------

    /**
     * Points every avatar on the page at the stored file, or the fallback when there
     * is none. The stored name is stable per user, so a changed picture would other-
     * wise keep showing from cache; the timestamp forces a refetch.
     */
    function showAvatar(avatarUrl, bustCache) {
        const src = avatarUrl
            ? avatarUrl + (bustCache ? '?v=' + Date.now() : '')
            : FALLBACK_AVATAR;

        document.querySelectorAll('.user-avatar').forEach(function (img) {
            img.src = src;
        });
        const removeBtn = document.getElementById('avatarRemoveBtn');
        if (removeBtn) {
            removeBtn.classList.toggle('d-none', !avatarUrl);
        }
    }

    /** Keeps the cached identity and the topbar in step after an avatar change. */
    function onAvatarChanged(user, message) {
        localStorage.setItem('sm.user', JSON.stringify(user));
        showAvatar(user.avatarUrl, true);
        UI.toast(message, 'success');
    }

    const avatarInput = document.getElementById('avatarInput');
    const pickBtn = document.getElementById('avatarPickBtn');
    const removeBtn = document.getElementById('avatarRemoveBtn');

    if (pickBtn && avatarInput) {
        pickBtn.addEventListener('click', function () {
            avatarInput.click();
        });

        avatarInput.addEventListener('change', function () {
            const file = avatarInput.files && avatarInput.files[0];
            if (!file) {
                return;
            }
            // Checked here as well as on the server: rejecting a 10 MB file before
            // uploading it saves the user the wait, but the server still decides.
            if (file.size > MAX_AVATAR_BYTES) {
                UI.toast('Image must be 2 MB or smaller', 'danger');
                avatarInput.value = '';
                return;
            }

            // Not UI.busy(): that swaps in a spinner plus a text label, which does not
            // fit an icon-only circular button. Same idea, just the icon.
            const icon = pickBtn.innerHTML;
            pickBtn.disabled = true;
            pickBtn.innerHTML =
                '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';

            Api.upload('/api/v1/auth/me/avatar', file)
                .then(function (user) {
                    onAvatarChanged(user, 'Profile picture updated');
                })
                .catch(function (error) {
                    UI.toast(error.message || 'Could not upload the image', 'danger');
                })
                .finally(function () {
                    pickBtn.disabled = false;
                    pickBtn.innerHTML = icon;
                    // Cleared so picking the same file again still fires a change event.
                    avatarInput.value = '';
                });
        });
    }

    if (removeBtn) {
        removeBtn.addEventListener('click', function () {
            UI.confirmDialog({
                title: 'Remove profile picture?',
                message: 'The default picture will be shown instead.',
                okText: 'Remove'
            }).then(function (confirmed) {
                if (!confirmed) {
                    return;
                }
                const restore = UI.busy(removeBtn, 'Removing...');
                Api.del('/api/v1/auth/me/avatar')
                    .then(function (user) {
                        onAvatarChanged(user, 'Profile picture removed');
                    })
                    .catch(function (error) {
                        UI.toast(error.message || 'Could not remove the image', 'danger');
                    })
                    .finally(restore);
            });
        });
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
