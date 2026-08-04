/**
 * Add-user form.
 *
 * The role list and the school field both depend on who is signed in. A super admin
 * appoints administrators: a school admin, who must be given a school, or another super
 * admin, who has none. A school admin adds its own staff and families, who join its school
 * automatically. Anything wider is rejected by the API, so the options here mirror it.
 */
(function () {
    'use strict';

    const form = document.getElementById('addUserForm');
    const roleSelect = document.getElementById('role');
    const schoolField = document.getElementById('schoolField');
    const schoolSelect = document.getElementById('schoolId');

    let superAdmin = false;

    function value(id) {
        const raw = document.getElementById(id).value.trim();
        return raw === '' ? null : raw;
    }

    /** A school admin belongs to a school; a super admin does not. */
    function schoolNeeded() {
        return superAdmin && roleSelect.value === 'SCHOOL_ADMIN';
    }

    function syncSchoolField() {
        const needed = schoolNeeded();
        schoolField.classList.toggle('d-none', !needed);
        schoolSelect.required = needed;
        if (!needed) {
            schoolSelect.value = '';
            schoolSelect.classList.remove('is-invalid');
        }
    }

    async function setUpForRole() {
        superAdmin = Shell.isSuperAdmin();

        const roles = superAdmin
            ? [
                {id: 'SCHOOL_ADMIN', name: 'School Admin'},
                {id: 'SUPER_ADMIN', name: 'Super Admin'}
            ]
            : [
                {id: 'TEACHER', name: 'Teacher'},
                {id: 'STUDENT', name: 'Student'},
                {id: 'PARENT', name: 'Parent'}
            ];
        UI.fillSelect(roleSelect, roles, {placeholder: 'Select role'});

        if (!superAdmin) {
            return;
        }

        // Retitled to match the nav: this form appoints administrators for a super admin.
        const heading = document.querySelector('.page-header h1');
        if (heading) {
            heading.textContent = 'Appoint Administrator';
        }

        roleSelect.addEventListener('change', syncSchoolField);
        syncSchoolField();

        try {
            const schools = await Api.get('/api/v1/schools');
            UI.fillSelect(schoolSelect, schools, {placeholder: 'Select school'});
            if (!schools.length) {
                UI.toast('Add a school before appointing a school admin.', 'warning');
            }
        } catch (error) {
            UI.toast('Could not load the school list', 'danger');
        }
    }

    UI.bindForm(form, {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: function () {
            const payload = {
                fullName: value('fullName'),
                username: value('username'),
                email: value('email'),
                password: document.getElementById('password').value,
                role: value('role'),
                phoneNumber: value('phoneNumber')
            };
            if (schoolNeeded()) {
                payload.schoolId = value('schoolId');
            }
            return Api.post('/api/v1/users', payload);
        },
        onSuccess: function () {
            UI.toast('User created', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/users/all';
            }, 700);
        }
    });

    // Both admin tiers create accounts: a super admin appoints administrators, a school
    // admin adds its own staff and families. setUpForRole shapes the form for each.
    Shell.requireAdmin()
        .then(setUpForRole)
        .catch(function () { /* redirect already under way */ });
})();
