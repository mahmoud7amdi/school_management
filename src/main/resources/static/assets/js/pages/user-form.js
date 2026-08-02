/**
 * Add-user form.
 *
 * The role list and the school field both depend on who is signed in: a super
 * admin creates school admins and must pick a school; a school admin creates
 * staff, who join their own school automatically.
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

    async function setUpForRole() {
        superAdmin = Shell.isSuperAdmin();

        const roles = superAdmin
            ? [{id: 'SCHOOL_ADMIN', name: 'School Admin'}]
            : [
                {id: 'TEACHER', name: 'Teacher'},
                {id: 'STUDENT', name: 'Student'},
                {id: 'PARENT', name: 'Parent'}
            ];
        UI.fillSelect(roleSelect, roles, {placeholder: 'Select role'});

        if (!superAdmin) {
            return;
        }

        schoolField.classList.remove('d-none');
        schoolSelect.required = true;
        try {
            const schools = await Api.get('/api/v1/schools');
            UI.fillSelect(schoolSelect, schools, {placeholder: 'Select school'});
            if (!schools.length) {
                UI.toast('Add a school before creating a school admin.', 'warning');
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
            if (superAdmin) {
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

    Shell.requireManager()
        .then(setUpForRole)
        .catch(function () { /* redirect already under way */ });
})();
