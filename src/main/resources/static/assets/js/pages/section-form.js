/** Add section. The school comes from the selected grade. */
(function () {
    'use strict';

    UI.bindForm(document.getElementById('sectionForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/sections', {
            name: document.getElementById('name').value.trim(),
            capacity: document.getElementById('capacity').value || null,
            description: document.getElementById('description').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            sectionHeadId: document.getElementById('sectionHeadId').value || null
        }),
        onSuccess: function () {
            UI.toast('Section added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/sections/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [grades, teachers] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/teachers')
            ]);

            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            UI.fillSelect(document.getElementById('sectionHeadId'), teachers, {
                placeholder: 'No section head',
                label: (teacher) => teacher.fullName
            });

            if (!grades.length) {
                UI.toast('Create a grade before adding sections.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
