/** New enrollment. The school comes from the selected student. */
(function () {
    'use strict';

    let classrooms = [];

    UI.bindForm(document.getElementById('enrollmentForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/enrollments', {
            rollNumber: document.getElementById('rollNumber').value.trim() || null,
            enrollmentDate: document.getElementById('enrollmentDate').value,
            completionDate: null,
            status: document.getElementById('status').value,
            remarks: document.getElementById('remarks').value.trim() || null,
            studentId: document.getElementById('studentId').value,
            academicYearId: document.getElementById('academicYearId').value,
            gradeId: document.getElementById('gradeId').value,
            classroomId: document.getElementById('classroomId').value || null
        }),
        onSuccess: function () {
            UI.toast('Enrollment recorded', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/enrollments/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [students, years, grades, rooms] = await Promise.all([
                Api.get('/api/v1/students'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/classrooms')
            ]);
            classrooms = rooms;

            UI.fillSelect(document.getElementById('studentId'), students, {
                placeholder: 'Select student',
                label: (student) => student.fullName + ' (' + student.admissionNumber + ')'
            });
            UI.fillSelect(document.getElementById('academicYearId'), years, {
                placeholder: 'Select academic year',
                label: (year) => showSchool && year.school
                    ? year.name + ' — ' + year.school.name
                    : year.name
            });
            UI.fillSelect(document.getElementById('gradeId'), grades, {
                placeholder: 'Select grade',
                label: (grade) => showSchool && grade.school
                    ? grade.name + ' — ' + grade.school.name
                    : grade.name
            });

            const refreshClassrooms = EnrollmentForm.bindClassroomFilter({
                classrooms: () => classrooms
            });
            refreshClassrooms();

            // Default the year to whichever is marked current, and the date to today.
            const current = years.find((year) => year.current);
            if (current) {
                document.getElementById('academicYearId').value = current.id;
                refreshClassrooms();
            }
            document.getElementById('enrollmentDate').value = new Date().toISOString().slice(0, 10);

            if (!students.length || !years.length || !grades.length) {
                UI.toast('Add students, grades and an academic year before enrolling.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
