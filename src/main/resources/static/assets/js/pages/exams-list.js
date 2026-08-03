/** Exams list. The edit modal needs subjects, years and classrooms. */
(function () {
    'use strict';

    const TYPE_CLASSES = {
        QUIZ: 'bg-primary-subtle text-primary-emphasis',
        ASSIGNMENT: 'bg-info-subtle text-info-emphasis',
        MIDTERM: 'bg-warning-subtle text-warning-emphasis',
        FINAL: 'bg-danger-subtle text-danger-emphasis',
        PRACTICAL: 'bg-success-subtle text-success-emphasis',
        MOCK: 'bg-secondary-subtle text-secondary-emphasis'
    };

    let classrooms = [];

    function refreshClassrooms(selectedId) {
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
        const select = document.getElementById('classroomId');
        UI.fillSelect(select, matching, {
            placeholder: 'Whole grade',
            label: (classroom) => classroom.name
        });
        if (selectedId !== undefined) {
            select.value = selectedId === null ? '' : String(selectedId);
        }
    }

    function row(exam) {
        const title = UI.escapeHtml(exam.title);
        const classroomLabel = exam.classroom ? exam.classroom.name : 'Whole grade';
        return '<tr>' +
            '<td>' +
                '<span class="fw-semibold">' + title + '</span>' +
                '<div class="small text-secondary">' + UI.escapeHtml(classroomLabel) + '</div>' +
            '</td>' +
            '<td>' + UI.badge(exam.examTypeLabel || exam.examType,
                TYPE_CLASSES[exam.examType] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td>' + UI.dash(exam.subject ? exam.subject.name : null) + '</td>' +
            '<td>' + UI.dash(exam.grade ? exam.grade.name : null) + '</td>' +
            '<td>' + UI.dash(UI.formatDate(exam.examDate)) + '</td>' +
            '<td>' + UI.dash(exam.maxMarks) + ' / ' + UI.dash(exam.passMarks) + '</td>' +
            '<td>' + UI.badge(exam.resultCount + (exam.resultCount === 1 ? ' result' : ' results'),
                'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td class="school-col">' + UI.dash(exam.school ? exam.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<a class="btn btn-sm btn-ghost-primary" title="Enter marks" ' +
                    'href="/dashboard/exams/results?examId=' + exam.id + '">' +
                    '<i class="ti ti-list-numbers fs-5"></i></a>' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + exam.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + exam.id + '" data-name="' + title + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/exams',
        label: 'exam',
        colspan: 9,
        emptyIcon: 'ti-file-off',
        emptyText: 'No exams scheduled yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (exam) => [
            exam.title,
            exam.examTypeLabel,
            exam.subject ? exam.subject.name : null,
            exam.grade ? exam.grade.name : null,
            exam.classroom ? exam.classroom.name : null,
            exam.academicYear ? exam.academicYear.name : null,
            exam.school ? exam.school.name : null,
            String(exam.examDate)
        ],
        fillForm: function (exam) {
            document.getElementById('title').value = exam.title || '';
            document.getElementById('examType').value = exam.examType || 'QUIZ';
            document.getElementById('subjectId').value = exam.subject ? exam.subject.id : '';
            document.getElementById('academicYearId').value =
                exam.academicYear ? exam.academicYear.id : '';
            refreshClassrooms(exam.classroom ? exam.classroom.id : null);
            document.getElementById('examDate').value = exam.examDate || '';
            document.getElementById('startTime').value = exam.startTime || '';
            document.getElementById('durationMinutes').value = exam.durationMinutes || '';
            document.getElementById('maxMarks').value = exam.maxMarks || '';
            document.getElementById('passMarks').value = exam.passMarks || '';
            document.getElementById('description').value = exam.description || '';
        },
        toPayload: () => ({
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
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
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

            document.getElementById('subjectId').addEventListener('change', () => refreshClassrooms());
            document.getElementById('academicYearId').addEventListener('change', () => refreshClassrooms());

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load exams', 'danger');
            }
        });
})();
