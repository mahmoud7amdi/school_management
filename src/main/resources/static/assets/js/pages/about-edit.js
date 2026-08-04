/**
 * About page editor. Super admin only.
 *
 * Loads through the same public GET the visitors' page uses, so the editor always shows
 * exactly what is published — including the built-in defaults before the page has ever
 * been saved, which is what makes the first edit a change rather than a blank slate.
 */
(function () {
    'use strict';

    const form = document.getElementById('aboutForm');

    const FIELDS = ['title', 'tagline', 'body', 'mission',
        'contactEmail', 'contactPhone', 'address', 'website'];

    function value(id) {
        const el = document.getElementById(id);
        if (!el) {
            return null;
        }
        const raw = el.value.trim();
        return raw === '' ? null : raw;
    }

    function fill(about) {
        FIELDS.forEach(function (id) {
            const el = document.getElementById(id);
            if (el) {
                el.value = about[id] || '';
            }
        });

        const updated = document.getElementById('lastUpdated');
        if (updated) {
            updated.textContent = about.lastUpdated
                ? 'Last updated ' + about.lastUpdated +
                  (about.updatedBy ? ' by ' + about.updatedBy : '')
                : 'Not published yet — these are the default contents.';
        }
    }

    UI.bindForm(form, {
        submitButton: document.getElementById('saveAboutBtn'),
        onSubmit: function () {
            const payload = {};
            FIELDS.forEach(function (id) {
                payload[id] = value(id);
            });
            return Api.put('/api/v1/about', payload);
        },
        onSuccess: function (about) {
            UI.toast('About page updated', 'success');
            fill(about);
        }
    });

    Shell.requireRole('SUPER_ADMIN')
        .then(function () {
            return Api.get('/api/v1/about').then(fill);
        })
        .catch(function (error) {
            if (error && error.message === 'Insufficient role') {
                return;
            }
            UI.toast((error && error.message) || 'Could not load the About page', 'danger');
        });
})();
