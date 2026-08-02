/** Sign-in page. Stores the token, then honours any ?next= return path. */
(function () {
    'use strict';

    const form = document.getElementById('loginForm');
    const errorBox = document.getElementById('loginError');
    const button = document.getElementById('loginBtn');

    /** Only same-origin relative paths are accepted, so ?next= cannot redirect off-site. */
    function safeNext() {
        const next = new URLSearchParams(window.location.search).get('next');
        if (next && next.startsWith('/') && !next.startsWith('//')) {
            return next;
        }
        return '/dashboard/index';
    }

    function showError(message) {
        errorBox.textContent = message;
        errorBox.classList.remove('d-none');
    }

    function hideError() {
        errorBox.classList.add('d-none');
    }

    // An already-valid session skips the form entirely.
    if (localStorage.getItem(Api.TOKEN_KEY)) {
        window.location.replace(safeNext());
    }

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        hideError();

        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const original = button.innerHTML;
        button.disabled = true;
        button.innerHTML =
            '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>Signing in...';

        try {
            const data = await Api.post('/api/v1/auth/login', {
                username: document.getElementById('username').value.trim(),
                password: document.getElementById('password').value
            }, {redirectOnUnauthorized: false});

            localStorage.setItem(Api.TOKEN_KEY, data.token);
            localStorage.setItem('sm.user', JSON.stringify(data.user));
            window.location.replace(safeNext());
        } catch (error) {
            button.disabled = false;
            button.innerHTML = original;
            showError(error.message || 'Sign in failed. Please try again.');
        }
    });

    form.querySelectorAll('input').forEach(function (input) {
        input.addEventListener('input', hideError);
    });
})();
