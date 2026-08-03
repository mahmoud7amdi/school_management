/** Enrollments list. The edit modal needs students, years, grades and classrooms. */
(function () {
    'use strict';

    const STATUS_CLASSES = {
        ENROLLED: 'bg-success-subtle text-success-emphasis',
        COMPLETED: 'bg-primary-subtle text-primary-emphasis',
        REPEATING: 'bg-warning-subtle text-warning-emphasis',
        WITHDRAWN: 'bg-danger-subtle text-danger-emphasis',
        TRANSFERRED: 'bg-secondary-subtle text-secondary-emphasis'
    };

    // Classrooms belong to a grade + year, so the modal's picker is refiltered
    // whenever either changes. Assigned once the reference data has loaded, which
    // is before any row can open the modal.
    let refreshClassroomFilter = function () {};

    function row(enrollment) {
        const studentName = UI.escapeHtml(enrollment.student ? enrollment.student.name : '—');
        const statusLabel = enrollment.statusLabel || enrollment.status;
        const yearName = enrollment.academicYear ? enrollment.academicYear.name : null;
        return '<tr>' +
            '<td>' +
                '<div class="d-flex flex-column">' +
                '<span class="fw-semibold">' + studentName + '</span>' +
                (enrollment.admissionNumber
                    ? '<span class="small text-secondary">' + UI.escapeHtml(enrollment.admissionNumber) + '</span>'
                    : '') +
                '</div>' +
            '</td>' +
            '<td>' + UI.dash(yearName) + '</td>' +
            '<td>' + UI.dash(enrollment.grade ? enrollment.grade.name : null) + '</td>' +
            '<td>' + UI.dash(enrollment.classroom ? enrollment.classroom.name : null) + '</td>' +
            '<td>' + UI.dash(enrollment.rollNumber) + '</td>' +
            '<td>' + UI.dash(UI.formatDate(enrollment.enrollmentDate)) + '</td>' +
            '<td>' + UI.badge(statusLabel,
                STATUS_CLASSES[enrollment.status] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td class="school-col">' + UI.dash(enrollment.school ? enrollment.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + enrollment.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + enrollment.id + '" data-name="' + studentName + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/enrollments',
        label: 'enrollment',
        colspan: 9,
        emptyIcon: 'ti-clipboard-off',
        emptyText: 'No enrollments yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (enrollment) => [
            enrollment.student ? enrollment.student.name : null,
            enrollment.admissionNumber,
            enrollment.academicYear ? enrollment.academicYear.name : null,
            enrollment.grade ? enrollment.grade.name : null,
            enrollment.classroom ? enrollment.classroom.name : null,
            enrollment.rollNumber,
            enrollment.statusLabel,
            enrollment.school ? enrollment.school.name : null
        ],
        fillForm: function (enrollment) {
            document.getElementById('studentId').value = enrollment.student ? enrollment.student.id : '';
            document.getElementById('academicYearId').value =
                enrollment.academicYear ? enrollment.academicYear.id : '';
            document.getElementById('gradeId').value = enrollment.grade ? enrollment.grade.id : '';
            refreshClassroomFilter(enrollment.classroom ? enrollment.classroom.id : null);
            document.getElementById('rollNumber').value = enrollment.rollNumber || '';
            document.getElementById('enrollmentDate').value = enrollment.enrollmentDate || '';
            document.getElementById('completionDate').value = enrollment.completionDate || '';
            document.getElementById('status').value = enrollment.status || 'ENROLLED';
            document.getElementById('remarks').value = enrollment.remarks || '';
        },
        toPayload: () => ({
            rollNumber: document.getElementById('rollNumber').value.trim() || null,
            enrollmentDate: document.getElementById('enrollmentDate').value,
            completionDate: document.getElementById('completionDate').value || null,
            status: document.getElementById('status').value,
            remarks: document.getElementById('remarks').value.trim() || null,
            studentId: document.getElementById('studentId').value,
            academicYearId: document.getElementById('academicYearId').value,
            gradeId: document.getElementById('gradeId').value,
            classroomId: document.getElementById('classroomId').value || null
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
            const [students, years, grades, rooms] = await Promise.all([
                Api.get('/api/v1/students'),
                Api.get('/api/v1/academic-years'),
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/classrooms')
            ]);

            refreshClassroomFilter = EnrollmentForm.bindClassroomFilter({
                classrooms: () => rooms
            });

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

            // bindClassroomFilter registers its own grade/year change listeners.
            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load enrollments', 'danger');
            }
        });
})();
