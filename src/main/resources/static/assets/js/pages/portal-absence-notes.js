/**
 * Absence notes — one page, two modes.
 *
 * A student or parent files a note and watches for a decision; a teacher works the review
 * queue for their own classes. The two roles read different endpoints but the same table,
 * so the columns that only make sense for one of them are gated with data-requires-role
 * and removed by shell.js before this runs.
 */
(function () {
    'use strict';

    const tbody = document.getElementById('tableBody');
    const summary = document.getElementById('resultSummary');
    const pageTitle = document.getElementById('pageTitle');
    const picker = document.getElementById('childPicker');
    const pickerWrapper = document.getElementById('childPickerWrapper');
    const newNoteBtn = document.getElementById('newNoteBtn');

    const noteForm = document.getElementById('noteForm');
    const reviewForm = document.getElementById('reviewForm');

    let teacherMode = false;
    let currentStudentId = null;
    let reviewingId = null;

    const STATUS_CLASSES = {
        SUBMITTED: 'bg-warning-subtle text-warning-emphasis',
        ACKNOWLEDGED: 'bg-success-subtle text-success-emphasis',
        REJECTED: 'bg-danger-subtle text-danger-emphasis'
    };

    /** Columns differ by role, so the empty/error colspan has to match what survived. */
    function colspan() {
        const header = document.querySelector('table.app-table thead tr');
        return header ? header.children.length : 6;
    }

    function rowHtml(note) {
        const pending = note.status === 'SUBMITTED';
        return '<tr>' +
            (teacherMode
                ? '<td class="fw-semibold">' + UI.dash(note.student ? note.student.name : null) +
                '<div class="small text-secondary">' + UI.dash(note.admissionNumber) + '</div></td>'
                : '') +
            '<td>' + UI.escapeHtml(UI.formatDate(note.absenceDate) || '') + '</td>' +
            '<td class="text-wrap" style="max-width:22rem">' + UI.dash(note.reason) + '</td>' +
            '<td>' + UI.badge(note.statusLabel || note.status || '',
                STATUS_CLASSES[note.status] || 'bg-secondary-subtle text-secondary-emphasis') + '</td>' +
            '<td>' + UI.dash(note.reviewedBy ? note.reviewedBy.name : null) + '</td>' +
            '<td class="text-wrap" style="max-width:16rem">' + UI.dash(note.reviewNote) + '</td>' +
            (teacherMode
                ? '<td class="text-end">' +
                (pending
                    ? '<button type="button" class="btn btn-sm btn-ghost-primary" data-action="review" ' +
                    'data-id="' + note.id + '" title="Review">' +
                    '<i class="ti ti-gavel me-1"></i>Review</button>'
                    : '') +
                '</td>'
                : '') +
            '</tr>';
    }

    function render(notes) {
        notes = notes || [];
        if (notes.length === 0) {
            summary.textContent = teacherMode ? 'No notes to review.' : 'No notes submitted yet.';
            UI.table.empty(tbody, colspan(), teacherMode
                ? 'No absence notes have been submitted for your classes.'
                : 'You have not submitted any absence notes yet.', 'ti-note-off');
            return;
        }

        const pending = notes.filter((note) => note.status === 'SUBMITTED').length;
        summary.textContent = notes.length + ' note' + (notes.length === 1 ? '' : 's')
            + (pending > 0 ? ' · ' + pending + ' awaiting review' : '');
        tbody.innerHTML = notes.map(rowHtml).join('');
    }

    function load(studentId) {
        if (studentId !== undefined) {
            currentStudentId = studentId;
        }
        UI.table.loading(tbody, colspan(), 'Loading notes...');

        const url = teacherMode
            ? '/api/v1/portal/teacher/absence-notes'
            : Portal.scopedUrl('/api/v1/portal/absence-notes', currentStudentId);

        return Api.get(url)
            .then(render)
            .catch(function (error) {
                summary.textContent = 'Could not load notes.';
                UI.table.error(tbody, colspan(), error.message || 'Failed to load notes.');
            });
    }

    // --- submit (student / parent) -------------------------------------------

    if (newNoteBtn) {
        newNoteBtn.addEventListener('click', function () {
            UI.clearErrors(noteForm);
            noteForm.reset();
            // Absences are explained after the fact, so default to today and disallow
            // future dates rather than letting a family pre-book one.
            const today = new Date().toISOString().slice(0, 10);
            const dateInput = document.getElementById('absenceDate');
            dateInput.value = today;
            dateInput.max = today;
            bootstrap.Modal.getOrCreateInstance(document.getElementById('noteModal')).show();
        });
    }

    UI.bindForm(noteForm, {
        submitButton: document.getElementById('saveNoteBtn'),
        busyLabel: 'Submitting...',
        onSubmit: function () {
            return Api.post('/api/v1/portal/absence-notes', {
                studentId: currentStudentId,
                absenceDate: document.getElementById('absenceDate').value,
                reason: document.getElementById('reason').value
            });
        },
        onSuccess: function () {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('noteModal')).hide();
            UI.toast('Absence note submitted.', 'success');
            load();
        }
    });

    // --- review (teacher) ----------------------------------------------------

    tbody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action="review"]');
        if (!button) {
            return;
        }
        const row = button.closest('tr');
        reviewingId = button.getAttribute('data-id');

        UI.clearErrors(reviewForm);
        reviewForm.reset();
        document.getElementById('reviewStudent').textContent = row.children[0].innerText.trim();
        document.getElementById('reviewDate').textContent = row.children[1].innerText.trim();
        document.getElementById('reviewReason').textContent = row.children[2].innerText.trim();
        bootstrap.Modal.getOrCreateInstance(document.getElementById('reviewModal')).show();
    });

    UI.bindForm(reviewForm, {
        submitButton: document.getElementById('saveReviewBtn'),
        busyLabel: 'Saving...',
        onSubmit: function () {
            return Api.put('/api/v1/portal/absence-notes/' + reviewingId + '/review', {
                status: document.getElementById('status').value,
                reviewNote: document.getElementById('reviewNote').value
            });
        },
        onSuccess: function () {
            bootstrap.Modal.getOrCreateInstance(document.getElementById('reviewModal')).hide();
            UI.toast('Decision saved.', 'success');
            load();
        }
    });

    // --- boot ----------------------------------------------------------------

    Shell.requireRole(['TEACHER', 'STUDENT', 'PARENT'])
        .then(function () {
            teacherMode = Shell.isTeacher();
            if (teacherMode) {
                pageTitle.textContent = 'Absence Notes to Review';
                return load();
            }

            return Portal.initStudentContext({
                picker: picker,
                pickerWrapper: pickerWrapper,
                onChange: load
            }).then(function (context) {
                if (Shell.isParent() && context.children.length === 0) {
                    summary.textContent = 'No children are linked to your account yet.';
                    UI.table.empty(tbody, colspan(),
                        'No children are linked to your account. Contact the school office.', 'ti-users');
                    if (newNoteBtn) {
                        newNoteBtn.disabled = true;
                    }
                    return null;
                }
                return load(context.studentId);
            });
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load absence notes', 'danger');
            }
        });
})();
