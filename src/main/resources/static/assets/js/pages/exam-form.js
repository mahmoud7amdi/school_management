/** Add exam. The grade comes from the subject; the classroom must match. */
(function () {
    'use strict';

    let classrooms = [];

    // A classroom belongs to one grade and one year. The subject fixes the grade,
    // so the picker narrows by subject-grade and the chosen year.
    function refreshClassrooms() {
        const gradeId = document.getElementById('subjectId').selectedOptions[0]
            ? document.getElementById('subjectId').selectedOptions[0].getAttribute('data-grade')
            : '';
        const yearId = document.getElementById('academicYearId').value;
        const matching = classrooms.filter(function (classroom) {
            const gradeOk = !gradeId
                || (classroom.grade && String(classroom.grade.id) === String(gradeId));
            const yearOk = !yearId
                || (classroom.academicYear && String(classroom.academicYear.id) === String(yearId));
            return gradeOk && yearOk;
        });
        UI.fillSelect(document.getElementById('classroomId'), matching, {
            placeholder: 'Whole grade',
            label: (classroom) => classroom.name
        });
    }

    UI.bindForm(document.getElementById('examForm'), {
        submitButton: document.getElementById('saveBtn'),
        onSubmit: () => Api.post('/api/v1/exams', {
            title: document.getElementById('title').value.trim(),
            examType: document.getElementById('examType').value,
            examDate: document.getElementById('examDate').value,
            startTime: document.getElementById('startTime').value || null,
            durationMinutes: document.getElementById('durationMinutes').value || null,
            maxMarks: document.getElementById('maxMarks').value,
            passMarks: document.getElementById('passMarks').value,
            description: document.getElementById('description').value.trim() || null,
            subjectId: document.getElementById('subjectId').value,
            classroomId: document.getElementById('classroomId').value || null,
            academicYearId: document.getElementById('academicYearId').value
        }),
        onSuccess: function () {
            UI.toast('Exam added', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/exams/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            const showSchool = Shell.isSuperAdmin();
            const [subjects, years, rooms] = await Promise.all([
                Api.get('/api/v1/subjects'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/classrooms')
            ]);
            classrooms = rooms;

            UI.fillSelect(document.getElementById('subjectId'), subjects, {
                placeholder: 'Select subject',
                label: (subject) => showSchool && subject.school
                    ? subject.name + ' — ' + (subject.grade ? subject.grade.name + ', ' : '') + subject.school.name
                    : subject.name
            });
            // Keep the grade alongside each option for the classroom filter.
            document.querySelectorAll('#subjectId option').forEach(function (option) {
                const subject = subjects.find((s) => String(s.id) === option.value);
                if (subject && subject.grade) {
                    option.setAttribute('data-grade', subject.grade.id);
                }
            });

            UI.fillSelect(document.getElementById('academicYearId'), years, {
                placeholder: 'Select academic year',
                label: (year) => showSchool && year.school
                    ? year.name + ' — ' + year.school.name
                    : year.name
            });

            document.getElementById('subjectId').addEventListener('change', refreshClassrooms);
            document.getElementById('academicYearId').addEventListener('change', refreshClassrooms);

            const current = years.find((year) => year.current);
            if (current) {
                document.getElementById('academicYearId').value = current.id;
                refreshClassrooms();
            }
            document.getElementById('examDate').value = new Date().toISOString().slice(0, 10);

            if (!subjects.length || !years.length) {
                UI.toast('Add subjects and an academic year before creating exams.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
