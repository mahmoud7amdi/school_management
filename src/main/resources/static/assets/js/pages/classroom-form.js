/** Add classroom. Grade + academic year must belong to the same school. */
(function () {
    'use strict';

    // Sections belong to a grade, so the picker is refiltered whenever the grade
    // changes rather than listing every section in the school.
    let allSections = [];

    function fillSectionsForGrade() {
        const gradeId = document.getElementById('gradeId').value;
        const matching = allSections.filter((section) =>
            section.grade && String(section.grade.id) === String(gradeId));
        UI.fillSelect(document.getElementById('sectionId'), matching, {
            placeholder: 'No section',
            label: (section) => section.name
        });
    }

    UI.bindForm(document.getElementById('classroomForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/classrooms', {
            name: document.getElementById('name').value.trim(),
            capacity: document.getElementById('capacity').value || null,
            roomNumber: document.getElementById('roomNumber').value.trim() || null,
            gradeId: document.getElementById('gradeId').value,
            academicYearId: document.getElementById('academicYearId').value,
            classTeacherId: document.getElementById('classTeacherId').value || null,
            sectionId: document.getElementById('sectionId').value || null
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
            const [grades, years, teachers, sections] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/users/teachers'),
                Api.get('/api/v1/sections')
            ]);
            allSections = sections;

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

            document.getElementById('gradeId').addEventListener('change', fillSectionsForGrade);
            fillSectionsForGrade();

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
