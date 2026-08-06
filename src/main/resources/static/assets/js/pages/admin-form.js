/**
 * Appoint-administrator form. Super admin only.
 *
 * The role is fixed to SCHOOL_ADMIN rather than picked: appointing an administrator is the
 * only thing this page does. Teachers, students and parents are added through their own
 * pages, which create the person's sign-in along with the record, and the API rejects a
 * super admin creating any of them anyway.
 */
(function () {
    'use strict';

    const form = document.getElementById('addAdminForm');
    const schoolSelect = document.getElementById('schoolId');

    function value(id) {
        const raw = document.getElementById(id).value.trim();
        return raw === '' ? null : raw;
    }

    async function loadSchools() {
        try {
            const schools = await Api.get('/api/v1/schools');
            UI.fillSelect(schoolSelect, schools, {placeholder: 'Select school'});
            if (!schools.length) {
                UI.toast('Add a school before appointing an administrator.', 'warning');
            }
        } catch (error) {
            UI.toast('Could not load the school list', 'danger');
        }
    }

    UI.bindForm(form, {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/users', {
            fullName: value('fullName'),
            username: value('username'),
            email: value('email'),
            password: document.getElementById('password').value,
            role: 'SCHOOL_ADMIN',
            phoneNumber: value('phoneNumber'),
            schoolId: value('schoolId'),
            jobTitle: value('jobTitle'),
            department: value('department'),
            office: value('office'),
            appointmentDate: value('appointmentDate')
        }),
        onSuccess: function () {
            UI.toast('Administrator appointed', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/admins/all';
            }, 700);
        }
    });

    Shell.requireRole(['SUPER_ADMIN'])
        .then(loadSchools)
        .catch(function () { /* redirect already under way */ });
})();
