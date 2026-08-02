/** Add classroom. Grade + academic year must belong to the same school. */
(function () {
    'use strict';

    UI.bindForm(document.getElementById('classroomForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/classrooms', {
            name: document.getElementById('name').value.trim(),
            capacity: document.getElementById('capacity').value || null,
            roomNumber: document.getElementById('roomNumber').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            academicYearId: document.getElementById('academicYearId').value,
            classTeacherId: document.getElementById('classTeacherId').value || null
        }),
        onSuccess: function () {
            UI.toast('Classroom added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/classrooms/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [grades, years, teachers] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/users/teachers')
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
            UI.fillSelect(document.getElementById('classTeacherId'), teachers, {
                placeholder: 'No class teacher',
                label: (teacher) => teacher.fullName
            });

            if (!grades.length || !years.length) {
                UI.toast('Add a grade and an academic year before creating classrooms.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
