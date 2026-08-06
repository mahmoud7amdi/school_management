/**
 * Parent create/edit form. One page serves both, switched by ?id= in the URL.
 *
 * The child rows are the point of the page: a parent account can only ever see the
 * children linked here, so the picker is what grants portal access to a family.
 */
(function () {
    'use strict';

    const RELATIONSHIPS = [
        {value: 'MOTHER', label: 'Mother'},
        {value: 'FATHER', label: 'Father'},
        {value: 'GUARDIAN', label: 'Guardian'},
        {value: 'OTHER', label: 'Other'}
    ];

    const parentId = new URLSearchParams(window.location.search).get('id');
    const isEdit = !!parentId;

    const form = document.getElementById('parentForm');
    const childRows = document.getElementById('childRows');
    const noChildrenHint = document.getElementById('noChildrenHint');

    let students = [];

    function value(id) {
        const el = document.getElementById(id);
        return el && el.value.trim() !== '' ? el.value.trim() : null;
    }

    /** One child row: student, relationship, primary flag, remove. */
    function addChildRow(preset) {
        preset = preset || {};
        const row = document.createElement('div');
        row.className = 'border rounded p-3 child-row';
        row.innerHTML =
            '<div class="row g-2 align-items-end">' +
            '<div class="col-12">' +
            '<label class="form-label small">Child <span class="text-danger">*</span></label>' +
            '<select class="form-select form-select-sm child-student" required></select>' +
            '</div>' +
            '<div class="col-7">' +
            '<label class="form-label small">Relationship <span class="text-danger">*</span></label>' +
            '<select class="form-select form-select-sm child-relationship">' +
            RELATIONSHIPS.map((r) => '<option value="' + r.value + '">' + r.label + '</option>').join('') +
            '</select>' +
            '</div>' +
            '<div class="col-5 d-flex justify-content-between align-items-center">' +
            '<div class="form-check mb-1">' +
            '<input type="checkbox" class="form-check-input child-primary" id="primary-' + Date.now() +
            Math.random().toString(36).slice(2) + '" />' +
            '<label class="form-check-label small">Primary</label>' +
            '</div>' +
            '<button type="button" class="btn btn-sm btn-ghost-danger remove-child" title="Remove">' +
            '<i class="ti ti-trash"></i></button>' +
            '</div>' +
            '</div>';

        childRows.appendChild(row);

        UI.fillSelect(row.querySelector('.child-student'), students, {
            placeholder: 'Select a student',
            label: (student) => student.fullName +
                (student.admissionNumber ? ' (' + student.admissionNumber + ')' : '')
        });

        if (preset.studentId) {
            row.querySelector('.child-student').value = String(preset.studentId);
        }
        if (preset.relationship) {
            row.querySelector('.child-relationship').value = preset.relationship;
        }
        row.querySelector('.child-primary').checked = !!preset.primaryContact;

        updateHint();
    }

    function updateHint() {
        noChildrenHint.hidden = childRows.children.length > 0;
    }

    childRows.addEventListener('click', function (event) {
        if (event.target.closest('.remove-child')) {
            event.target.closest('.child-row').remove();
            updateHint();
        }
    });

    document.getElementById('addChildBtn').addEventListener('click', () => addChildRow());

    function collectChildren() {
        return Array.from(childRows.querySelectorAll('.child-row'))
            .map(function (row) {
                const studentId = row.querySelector('.child-student').value;
                if (!studentId) {
                    return null;
                }
                return {
                    studentId: Number(studentId),
                    relationship: row.querySelector('.child-relationship').value,
                    primaryContact: row.querySelector('.child-primary').checked
                };
            })
            .filter(Boolean);
    }

    function toPayload() {
        const payload = {
            firstName: value('firstName'),
            lastName: value('lastName'),
            email: value('email'),
            phoneNumber: value('phoneNumber'),
            occupation: value('occupation'),
            address: value('address'),
            children: collectChildren()
        };

        // Only a new guardian provisions a sign-in; an edit leaves the existing one alone.
        if (!isEdit) {
            payload.account = {
                username: value('accountUsername'),
                password: document.getElementById('accountPassword').value,
                active: document.getElementById('accountActive').checked
            };
        }

        return payload;
    }

    function fill(parent) {
        document.getElementById('firstName').value = parent.firstName || '';
        document.getElementById('lastName').value = parent.lastName || '';
        document.getElementById('email').value = parent.email || '';
        document.getElementById('phoneNumber').value = parent.phoneNumber || '';
        document.getElementById('occupation').value = parent.occupation || '';
        document.getElementById('address').value = parent.address || '';

        childRows.innerHTML = '';
        (parent.children || []).forEach(addChildRow);
        updateHint();
    }

    function switchToEditMode() {
        document.querySelector('h1').textContent = 'Edit Parent';
        document.title = 'Edit Parent | School Management';
        const crumb = document.querySelector('.breadcrumb-item.active');
        if (crumb) {
            crumb.textContent = 'Edit Parent';
        }
        document.getElementById('saveBtn').innerHTML =
            '<i class="ti ti-check me-1"></i> Save Changes';

        // The sign-in already exists, so the account fields are removed rather than
        // hidden — a hidden `required` input blocks submission and shows nothing.
        const accountSection = document.getElementById('accountSection');
        if (accountSection) {
            accountSection.remove();
        }
    }

    UI.bindForm(form, {
        submitButton: document.getElementById('saveBtn'),
        busyLabel: 'Saving...',
        onSubmit: function () {
            const payload = toPayload();
            return isEdit
                ? Api.put('/api/v1/parents/' + parentId, payload)
                : Api.post('/api/v1/parents', payload);
        },
        onSuccess: function () {
            UI.toast(isEdit ? 'Parent updated' : 'Parent added', 'success');
            window.location.assign('/dashboard/parents/all');
        }
    });

    Shell.requireManager()
        .then(async function () {
            students = (await Api.get('/api/v1/students/all')) || [];

            if (isEdit) {
                switchToEditMode();
                const parent = await Api.get('/api/v1/parents/' + parentId);
                fill(parent);
            } else {
                // Start with one row so the common case needs no extra click.
                addChildRow();
            }

            if (!students.length) {
                UI.toast('Enrol a student before linking a parent.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the parent form', 'danger');
            }
        });
})();
