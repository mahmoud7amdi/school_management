/** Add subject. The school comes from the selected grade. */
(function () {
    'use strict';

    UI.bindForm(document.getElementById('subjectForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/subjects', {
            name: document.getElementById('name').value.trim(),
            code: document.getElementById('code').value.trim() || null,
            weeklyHours: document.getElementById('weeklyHours').value || null,
            gradeId: document.getElementById('gradeId').value
        }),
        onSuccess: function () {
            UI.toast('Subject added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/subjects/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const grades = await Api.get('/api/v1/grades');
            const showSchool = Shell.isSuperAdmin();
            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            if (!grades.length) {
                UI.toast('Create a grade before adding subjects.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
