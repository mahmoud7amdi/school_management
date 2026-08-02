/**
 * Shared UI helpers: escaping, toasts, confirm dialog, form binding, table states.
 *
 * Vanilla DOM only — the vendor scripts are jQuery-free, so the dashboard is too.
 */
(function (window) {
    'use strict';

    // --- escaping ----------------------------------------------------------
    // Every dynamic string goes through here before reaching innerHTML.
    function escapeHtml(value) {
        if (value === null || value === undefined) {
            return '';
        }
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /** "John Doe" -> "JD". Falls back to "?" so the avatar is never blank. */
    function initials(name) {
        if (!name) {
            return '?';
        }
        const parts = String(name).trim().split(/\s+/).slice(0, 2);
        const letters = parts.map((part) => part.charAt(0)).join('');
        return letters ? letters.toUpperCase() : '?';
    }

    function dash(value) {
        if (value === null || value === undefined || value === '') {
            return '<span class="text-secondary">&mdash;</span>';
        }
        return escapeHtml(value);
    }

    /** Formats an ISO date (yyyy-mm-dd) for display, leaving junk untouched. */
    function formatDate(value) {
        if (!value) {
            return null;
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return String(value);
        }
        return date.toLocaleDateString(undefined, {year: 'numeric', month: 'short', day: 'numeric'});
    }

    // --- toasts ------------------------------------------------------------
    function toastContainer() {
        let container = document.querySelector('.toast-stack');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-stack';
            document.body.appendChild(container);
        }
        return container;
    }

    /**
     * Shows a dismissible toast.
     * @param {string} message text (escaped before insertion)
     * @param {'success'|'danger'|'warning'|'info'} [variant]
     */
    function toast(message, variant) {
        variant = variant || 'info';
        const icons = {
            success: 'ti-circle-check',
            danger: 'ti-alert-circle',
            warning: 'ti-alert-triangle',
            info: 'ti-info-circle'
        };

        const el = document.createElement('div');
        el.className = 'toast align-items-center border-0 text-bg-' + variant;
        el.setAttribute('role', variant === 'danger' ? 'alert' : 'status');
        el.setAttribute('aria-live', variant === 'danger' ? 'assertive' : 'polite');
        el.setAttribute('aria-atomic', 'true');
        el.innerHTML =
            '<div class="d-flex">' +
            '<div class="toast-body d-flex align-items-center gap-2">' +
            '<i class="ti ' + (icons[variant] || icons.info) + ' fs-5"></i>' +
            '<span>' + escapeHtml(message) + '</span>' +
            '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" ' +
            'data-bs-dismiss="toast" aria-label="Close"></button>' +
            '</div>';

        toastContainer().appendChild(el);
        const instance = bootstrap.Toast.getOrCreateInstance(el, {
            delay: variant === 'danger' ? 6000 : 3500
        });
        el.addEventListener('hidden.bs.toast', () => el.remove());
        instance.show();
    }

    // --- confirm dialog ----------------------------------------------------
    /**
     * Promise-based replacement for window.confirm, using the shared modal
     * fragment. Resolves true only when the confirm button was pressed.
     */
    function confirmDialog(options) {
        options = options || {};
        return new Promise(function (resolve) {
            const modalEl = document.getElementById('confirmModal');
            if (!modalEl) {
                resolve(window.confirm(options.message || 'Are you sure?'));
                return;
            }

            modalEl.querySelector('#confirmModalTitle').textContent = options.title || 'Are you sure?';
            modalEl.querySelector('#confirmModalBody').textContent =
                options.message || 'This action cannot be undone.';

            const okBtn = modalEl.querySelector('#confirmModalOkBtn');
            okBtn.textContent = options.okText || 'Delete';
            okBtn.className = 'btn ' + (options.okClass || 'btn-danger');

            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            let confirmed = false;

            function onOk() {
                confirmed = true;
                modal.hide();
            }

            function onHidden() {
                okBtn.removeEventListener('click', onOk);
                modalEl.removeEventListener('hidden.bs.modal', onHidden);
                resolve(confirmed);
            }

            okBtn.addEventListener('click', onOk);
            modalEl.addEventListener('hidden.bs.modal', onHidden);
            modal.show();
        });
    }

    // --- table states ------------------------------------------------------
    function tableState(tbody, colspan, icon, message, variant) {
        if (!tbody) {
            return;
        }
        tbody.innerHTML =
            '<tr><td colspan="' + colspan + '" class="table-state' +
            (variant ? ' text-' + variant : '') + '">' +
            '<i class="ti ' + icon + '"></i>' + escapeHtml(message) +
            '</td></tr>';
    }

    const table = {
        loading: (tbody, colspan, message) =>
            tableState(tbody, colspan, 'ti-loader-2', message || 'Loading...'),
        empty: (tbody, colspan, message, icon) =>
            tableState(tbody, colspan, icon || 'ti-database-off', message || 'Nothing here yet.'),
        error: (tbody, colspan, message) =>
            tableState(tbody, colspan, 'ti-alert-circle', message || 'Failed to load data.', 'danger')
    };

    // --- forms -------------------------------------------------------------

    /** Disables a submit button and swaps in a spinner; returns a restore function. */
    function busy(button, label) {
        if (!button) {
            return function () {};
        }
        const original = button.innerHTML;
        button.disabled = true;
        button.innerHTML =
            '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>' +
            escapeHtml(label || 'Saving...');
        return function restore() {
            button.disabled = false;
            button.innerHTML = original;
        };
    }

    /** Clears previous validation styling and messages from a form. */
    function clearErrors(form) {
        form.classList.remove('was-validated');
        form.querySelectorAll('.is-invalid').forEach((el) => el.classList.remove('is-invalid'));
        form.querySelectorAll('[data-server-feedback]').forEach((el) => {
            el.textContent = '';
        });
    }

    /**
     * Paints server-side field errors onto the matching inputs.
     *
     * Field names come from the DTO, so an input's id must match the property
     * name for its message to land on the right control; anything unmatched is
     * surfaced as a toast so it is never silently swallowed.
     */
    function applyFieldErrors(form, fieldErrors) {
        if (!fieldErrors) {
            return false;
        }
        let matched = false;
        Object.keys(fieldErrors).forEach(function (field) {
            const input = form.querySelector('[name="' + field + '"], #' + field);
            if (!input) {
                return;
            }
            matched = true;
            input.classList.add('is-invalid');
            const feedback = input.parentElement
                && input.parentElement.querySelector('[data-server-feedback]');
            if (feedback) {
                feedback.textContent = fieldErrors[field];
            }
        });
        return matched;
    }

    /**
     * Wires a form to a submit handler with HTML5 validation, a busy button and
     * uniform error reporting.
     *
     * @param {HTMLFormElement} form
     * @param {object} options {submitButton, busyLabel, onSubmit(), onSuccess()}
     */
    function bindForm(form, options) {
        if (!form) {
            return;
        }
        options = options || {};

        form.addEventListener('submit', async function (event) {
            event.preventDefault();
            clearErrors(form);

            if (!form.checkValidity()) {
                form.classList.add('was-validated');
                return;
            }

            const button = options.submitButton
                || form.querySelector('[type="submit"]');
            const restore = busy(button, options.busyLabel);

            try {
                const result = await options.onSubmit();
                if (options.onSuccess) {
                    options.onSuccess(result);
                }
                restore();
            } catch (error) {
                restore();
                const handled = applyFieldErrors(form, error.fieldErrors);
                if (handled) {
                    form.classList.add('was-validated');
                    toast(error.message || 'Please correct the highlighted fields', 'danger');
                } else {
                    toast(error.message || 'Something went wrong', 'danger');
                }
            }
        });
    }

    /** Fills a <select> from a list, preserving the current value when possible. */
    function fillSelect(select, items, config) {
        if (!select) {
            return;
        }
        config = config || {};
        const valueOf = config.value || ((item) => item.id);
        const labelOf = config.label || ((item) => item.name);
        const previous = select.value;

        const placeholder = config.placeholder !== undefined
            ? config.placeholder
            : 'Select an option';

        let html = placeholder === null
            ? ''
            : '<option value="">' + escapeHtml(placeholder) + '</option>';

        (items || []).forEach(function (item) {
            html += '<option value="' + escapeHtml(valueOf(item)) + '">' +
                escapeHtml(labelOf(item)) + '</option>';
        });

        select.innerHTML = html;
        if (previous) {
            select.value = previous;
        }
    }

    /** Badge markup for a value, given a class map. */
    function badge(text, className) {
        return '<span class="badge ' + (className || 'bg-secondary-subtle text-secondary-emphasis') +
            '">' + escapeHtml(text) + '</span>';
    }

    window.UI = {
        escapeHtml: escapeHtml,
        initials: initials,
        dash: dash,
        formatDate: formatDate,
        toast: toast,
        confirmDialog: confirmDialog,
        table: table,
        busy: busy,
        bindForm: bindForm,
        clearErrors: clearErrors,
        applyFieldErrors: applyFieldErrors,
        fillSelect: fillSelect,
        badge: badge
    };
})(window);
