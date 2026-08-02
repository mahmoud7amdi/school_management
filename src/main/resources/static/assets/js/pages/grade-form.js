/** Add grade. A super admin must nominate the school. */
(function () {
    'use strict';

    UI.bindForm(document.getElementById('gradeForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: function () {
            const payload = {
                name: document.getElementById('name').value.trim(),
                levelOrder: document.getElementById('levelOrder').value || null,
                description: document.getElementById('description').value.trim() || null
            };
            if (Shell.isSuperAdmin()) {
                payload.schoolId = document.getElementById('schoolId').value || null;
            }
            return Api.post('/api/v1/grades', payload);
        },
        onSuccess: function () {
            UI.toast('Grade added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/grades/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            if (!Shell.isSuperAdmin()) {
                return;
            }
            const select = document.getElementById('schoolId');
            document.getElementById('schoolField').classList.remove('d-none');
            select.required = true;

            const schools = await Api.get('/api/v1/schools');
            UI.fillSelect(select, schools, {placeholder: 'Select school'});
            if (!schools.length) {
                UI.toast('Add a school first.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
