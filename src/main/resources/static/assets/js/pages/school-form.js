/** Add-school form. Super admin only. */
(function () {
    'use strict';

    const form = document.getElementById('addSchoolForm');

    function value(id) {
        const raw = document.getElementById(id).value.trim();
        return raw === '' ? null : raw;
    }

    UI.bindForm(form, {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/schools', {
            name: value('name'),
            email: value('email'),
            phoneNumber: value('phoneNumber'),
            website: value('website'),
            address: value('address')
        }),
        onSuccess: function () {
            UI.toast('School added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/schools/all';
            }, 700);
        }
    });

    Shell.requireRole('SUPER_ADMIN').catch(function () { /* redirect under way */ });
})();
