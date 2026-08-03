/** Add fee item. The school comes from the selected grade. */
(function () {
    'use strict';

    UI.bindForm(document.getElementById('feeStructureForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/fees/structures', {
            name: document.getElementById('name').value.trim(),
            feeType: document.getElementById('feeType').value,
            amount: document.getElementById('amount').value,
            dueDate: document.getElementById('dueDate').value,
            description: document.getElementById('description').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            academicYearId: document.getElementById('academicYearId').value
        }),
        onSuccess: function () {
            UI.toast('Fee item added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/fees/structures';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [grades, years] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/academic-years')
            ]);

            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });
            UI.fillSelect(document.getElementById('academicYearId'), years, {
                placeholder: 'Select academic year',
                label: (year) => showSchool && year.school
                    ? year.name + ' — ' + year.school.name
                    : year.name
            });

            const current = years.find((year) => year.current);
            if (current) {
                document.getElementById('academicYearId').value = current.id;
            }

            if (!grades.length || !years.length) {
                UI.toast('Add a grade and an academic year before creating fee items.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
