/** Add teacher. The school comes from the caller's tenant. */
(function () {
    'use strict';

    function selectedSubjectIds() {
        return Array.from(document.getElementById('subjectIds').selectedOptions)
            .map((option) => option.value);
    }

    UI.bindForm(document.getElementById('teacherForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/teachers', {
            employeeNumber: document.getElementById('employeeNumber').value.trim(),
            firstName: document.getElementById('firstName').value.trim(),
            lastName: document.getElementById('lastName').value.trim(),
            gender: document.getElementById('gender').value,
            dateOfBirth: document.getElementById('dateOfBirth').value || null,
            email: document.getElementById('email').value.trim() || null,
            phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
            address: document.getElementById('address').value.trim() || null,
            qualification: document.getElementById('qualification').value.trim() || null,
            specialization: document.getElementById('specialization').value.trim() || null,
            hireDate: document.getElementById('hireDate').value,
            status: document.getElementById('status').value,
            subjectIds: selectedSubjectIds(),
            userAccountId: document.getElementById('userAccountId').value || null
        }),
        onSuccess: function () {
            UI.toast('Teacher added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/teachers/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const [subjects, users] = await Promise.all([
                Api.get('/api/v1/subjects'),
                Api.get('/api/v1/users/teachers')
            ]);

            UI.fillSelect(document.getElementById('subjectIds'), subjects, {
                placeholder: null,
                label: (subject) => subject.name
            });
            UI.fillSelect(document.getElementById('userAccountId'), users, {
                placeholder: 'No login account',
                label: (user) => user.fullName + ' (@' + user.username + ')'
            });

            if (!subjects.length) {
                UI.toast('Add subjects before assigning them to teachers.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
