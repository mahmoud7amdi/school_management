/** Teachers list. The edit modal needs subjects and login accounts, so it loads those first. */
(function () {
    'use strict';

    const STATUS_CLASSES = {
        ACTIVE: 'bg-success-subtle text-success-emphasis',
        ON_LEAVE: 'bg-warning-subtle text-warning-emphasis',
        SUSPENDED: 'bg-danger-subtle text-danger-emphasis',
        RESIGNED: 'bg-secondary-subtle text-secondary-emphasis',
        RETIRED: 'bg-secondary-subtle text-secondary-emphasis'
    };

    function subjectList(teacher) {
        if (!teacher.subjects || !teacher.subjects.length) {
            return '<span class="text-secondary">&mdash;</span>';
        }
        // Keep the cell short: name the first two and count the rest.
        const shown = teacher.subjects.slice(0, 2)
            .map((subject) => UI.badge(subject.name, 'bg-primary-subtle text-primary-emphasis'))
            .join(' ');
        const extra = teacher.subjects.length - 2;
        return shown + (extra > 0
            ? ' <span class="small text-secondary">+' + extra + '</span>'
            : '');
    }

    function contact(teacher) {
        const parts = [];
        if (teacher.email) {
            parts.push('<div class="small">' + UI.escapeHtml(teacher.email) + '</div>');
        }
        if (teacher.phoneNumber) {
            parts.push('<div class="small text-secondary">' + UI.escapeHtml(teacher.phoneNumber) + '</div>');
        }
        return parts.length ? parts.join('') : '<span class="text-secondary">&mdash;</span>';
    }

    function row(teacher) {
        const name = UI.escapeHtml(teacher.fullName);
        const accountTag = teacher.hasUserAccount
            ? ' <i class="ti ti-user-check text-success" title="Has a login"></i>'
            : '';
        return '<tr>' +
            '<td>' +
                '<div class="d-flex align-items-center gap-2">' +
                '<span class="avatar avatar-sm avatar-initial rounded-circle bg-primary-subtle text-primary-emphasis">' +
                UI.escapeHtml(UI.initials(teacher.fullName)) + '</span>' +
                '<div><span class="fw-semibold">' + name + '</span>' + accountTag +
                (teacher.specialization
                    ? '<div class="small text-secondary">' + UI.escapeHtml(teacher.specialization) + '</div>'
                    : '') +
                '</div></div>' +
            '</td>' +
            '<td>' + UI.dash(teacher.employeeNumber) + '</td>' +
            '<td>' + subjectList(teacher) + '</td>' +
            '<td>' + contact(teacher) + '</td>' +
            '<td>' + UI.dash(UI.formatDate(teacher.hireDate)) + '</td>' +
            '<td>' + UI.badge(teacher.statusLabel || teacher.status,
                STATUS_CLASSES[teacher.status] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td class="school-col">' + UI.dash(teacher.school ? teacher.school.name : null) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="edit" ' +
                    'data-id="' + teacher.id + '" title="Edit"><i class="ti ti-pencil fs-5"></i></button>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + teacher.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    function selectedSubjectIds() {
        return Array.from(document.getElementById('subjectIds').selectedOptions)
            .map((option) => option.value);
    }

    const page = CrudPage.create({
        endpoint: '/api/v1/teachers',
        label: 'teacher',
        colspan: 8,
        emptyIcon: 'ti-user-off',
        emptyText: 'No teachers yet.',
        tbody: document.getElementById('tableBody'),
        searchInput: document.getElementById('searchInput'),
        summary: document.getElementById('resultSummary'),
        form: document.getElementById('editForm'),
        modal: document.getElementById('editModal'),
        submitButton: document.getElementById('updateBtn'),
        row: row,
        searchFields: (teacher) => [
            teacher.fullName,
            teacher.employeeNumber,
            teacher.email,
            teacher.phoneNumber,
            teacher.qualification,
            teacher.specialization,
            teacher.statusLabel,
            teacher.school ? teacher.school.name : null
        ].concat((teacher.subjects || []).map((subject) => subject.name)),
        fillForm: function (teacher) {
            document.getElementById('employeeNumber').value = teacher.employeeNumber || '';
            document.getElementById('firstName').value = teacher.firstName || '';
            document.getElementById('lastName').value = teacher.lastName || '';
            document.getElementById('gender').value = teacher.gender || '';
            document.getElementById('dateOfBirth').value = teacher.dateOfBirth || '';
            document.getElementById('hireDate').value = teacher.hireDate || '';
            document.getElementById('email').value = teacher.email || '';
            document.getElementById('phoneNumber').value = teacher.phoneNumber || '';
            document.getElementById('address').value = teacher.address || '';
            document.getElementById('qualification').value = teacher.qualification || '';
            document.getElementById('specialization').value = teacher.specialization || '';
            document.getElementById('status').value = teacher.status || 'ACTIVE';

            const selected = new Set((teacher.subjects || []).map((subject) => String(subject.id)));
            Array.from(document.getElementById('subjectIds').options)
                .forEach((option) => { option.selected = selected.has(option.value); });
        },
        toPayload: () => ({
            employeeNumber: document.getElementById('employeeNumber').value.trim(),
            firstName: document.getElementById('firstName').value.trim(),
            lastName: document.getElementById('lastName').value.trim(),
            gender: document.getElementById('gender').value,
            dateOfBirth: document.getElementById('dateOfBirth').value || null,
            email: document.getElementById('email').value.trim(),
            phoneNumber: document.getElementById('phoneNumber').value.trim() || null,
            address: document.getElementById('address').value.trim() || null,
            qualification: document.getElementById('qualification').value.trim() || null,
            specialization: document.getElementById('specialization').value.trim() || null,
            hireDate: document.getElementById('hireDate').value,
            status: document.getElementById('status').value,
            subjectIds: selectedSubjectIds()
            // No account block: an edit leaves the teacher's existing sign-in untouched.
        }),
        onLoaded: function () {
            if (!Shell.isSuperAdmin()) {
                document.querySelectorAll('.school-col').forEach((el) => el.classList.add('d-none'));
            }
        }
    });

    Shell.requireManager()
        .then(async function () {
            const subjects = await Api.get('/api/v1/subjects');

            UI.fillSelect(document.getElementById('subjectIds'), subjects, {
                placeholder: null,
                label: (subject) => subject.name
            });

            return page.load();
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load teachers', 'danger');
            }
        });
})();
