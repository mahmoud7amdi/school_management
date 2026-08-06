/** Add teacher. The school comes from the caller's tenant, and the sign-in is created with the record. */
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
            email: document.getElementById('email').value.trim(),
            phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
            address: document.getElementById('address').value.trim() || null,
            qualification: document.getElementById('qualification').value.trim() || null,
            specialization: document.getElementById('specialization').value.trim() || null,
            hireDate: document.getElementById('hireDate').value,
            status: document.getElementById('status').value,
            subjectIds: selectedSubjectIds(),
            account: {
                username: document.getElementById('accountUsername').value.trim(),
                password: document.getElementById('accountPassword').value,
                active: document.getElementById('accountActive').checked
            }
        }),
        onSuccess: function () {
            UI.toast('Teacher added, and their sign-in was created', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/teachers/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const subjects = await Api.get('/api/v1/subjects');

            UI.fillSelect(document.getElementById('subjectIds'), subjects, {
                placeholder: null,
                label: (subject) => subject.name
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
