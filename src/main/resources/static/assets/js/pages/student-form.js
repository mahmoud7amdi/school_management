/**
 * Student create/edit form.
 *
 * One page serves both: `?id=` switches it to edit mode. The classroom list is
 * filtered by the chosen grade, because the backend rejects a classroom that
 * belongs to a different grade.
 */
(function () {
    'use strict';

    const form = document.getElementById('studentForm');
    const gradeSelect = document.getElementById('gradeId');
    const classroomSelect = document.getElementById('classroomId');
    const studentId = new URLSearchParams(window.location.search).get('id');
    const isEdit = !!studentId;

    let allClassrooms = [];

    function value(id) {
        const el = document.getElementById(id);
        const raw = el ? el.value.trim() : '';
        return raw === '' ? null : raw;
    }

    /** Classrooms are scoped to the selected grade; anything else fails server-side. */
    function refreshClassrooms(keepValue) {
        const gradeId = gradeSelect.value;
        if (!gradeId) {
            classroomSelect.innerHTML = '<option value="">Select a grade first</option>';
            classroomSelect.disabled = true;
            return;
        }

        const matches = allClassrooms.filter(
            (room) => room.grade && String(room.grade.id) === String(gradeId));

        classroomSelect.disabled = false;
        UI.fillSelect(classroomSelect, matches, {
            placeholder: matches.length ? 'No classroom yet' : 'No classrooms for this grade',
            label: (room) => room.name +
                (room.academicYear ? ' — ' + room.academicYear.name : '') +
                (room.capacity ? ' (' + room.studentCount + '/' + room.capacity + ')' : '')
        });

        if (keepValue) {
            classroomSelect.value = keepValue;
        }
    }

    function toPayload() {
        const payload = {
            admissionNumber: value('admissionNumber'),
            firstName: value('firstName'),
            lastName: value('lastName'),
            gender: value('gender'),
            dateOfBirth: value('dateOfBirth'),
            email: value('email'),
            phoneNumber: value('phoneNumber'),
            address: value('address'),
            guardianName: value('guardianName'),
            guardianPhone: value('guardianPhone'),
            guardianEmail: value('guardianEmail'),
            enrollmentDate: value('enrollmentDate'),
            status: value('status'),
            gradeId: value('gradeId'),
            classroomId: value('classroomId')
        };

        // Only a new enrolment provisions a sign-in; an edit leaves the existing one alone.
        if (!isEdit) {
            payload.account = {
                username: value('accountUsername'),
                password: document.getElementById('accountPassword').value,
                active: document.getElementById('accountActive').checked
            };
        }

        return payload;
    }

    function fill(student) {
        document.getElementById('admissionNumber').value = student.admissionNumber || '';
        document.getElementById('firstName').value = student.firstName || '';
        document.getElementById('lastName').value = student.lastName || '';
        document.getElementById('gender').value = student.gender || '';
        document.getElementById('dateOfBirth').value = student.dateOfBirth || '';
        document.getElementById('email').value = student.email || '';
        document.getElementById('phoneNumber').value = student.phoneNumber || '';
        document.getElementById('address').value = student.address || '';
        document.getElementById('guardianName').value = student.guardianName || '';
        document.getElementById('guardianPhone').value = student.guardianPhone || '';
        document.getElementById('guardianEmail').value = student.guardianEmail || '';
        document.getElementById('enrollmentDate').value = student.enrollmentDate || '';
        document.getElementById('status').value = student.status || 'ACTIVE';
        gradeSelect.value = student.grade ? student.grade.id : '';
        refreshClassrooms(student.classroom ? String(student.classroom.id) : '');
    }

    function switchToEditMode() {
        document.getElementById('pageTitle').textContent = 'Edit Student';
        document.getElementById('crumbAction').textContent = 'Edit';
        document.getElementById('saveLabel').textContent = 'Save Changes';
        document.title = 'Edit Student | School Management';

        // The sign-in already exists, so the account fields are removed rather than
        // hidden — a hidden `required` input blocks submission and shows nothing.
        const accountSection = document.getElementById('accountSection');
        if (accountSection) {
            accountSection.remove();
        }
    }

    async function loadReferenceData() {
        const [grades, classrooms] = await Promise.all([
            Api.get('/api/v1/grades'),
            Api.get('/api/v1/classrooms')
        ]);

        allClassrooms = classrooms;
        // A super admin sees several schools at once, so disambiguate the grade.
        const showSchool = Shell.isSuperAdmin();
        UI.fillSelect(gradeSelect, grades, {
            placeholder: 'Select grade',
            label: (grade) => showSchool && grade.school
                ? grade.name + ' — ' + grade.school.name
                : grade.name
        });

        if (!grades.length) {
            UI.toast('Create a grade before enrolling students.', 'warning');
        }
    }

    gradeSelect.addEventListener('change', () => refreshClassrooms());

    UI.bindForm(form, {
        submitButton: document.getElementById('saveBtn'),
        busyLabel: isEdit ? 'Saving...' : 'Enrolling...',
        onSubmit: () => isEdit
            ? Api.put('/api/v1/students/' + studentId, toPayload())
            : Api.post('/api/v1/students', toPayload()),
        onSuccess: function () {
            UI.toast(isEdit ? 'Student updated' : 'Student enrolled', 'success');
            window.setTimeout(function () {
                window.location.href = '/dashboard/students/all';
            }, 700);
        }
    });

    Shell.requireManager()
        .then(async function () {
            if (isEdit) {
                switchToEditMode();
            } else {
                // Default the enrolment date to today for a new record.
                document.getElementById('enrollmentDate').value =
                    new Date().toISOString().slice(0, 10);
            }

            await loadReferenceData();

            if (isEdit) {
                const student = await Api.get('/api/v1/students/' + studentId);
                fill(student);
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the form', 'danger');
            }
        });
})();
