// Shared helpers for the dashboard pages.
(function (window, $) {
    'use strict';

    // --- HTML escape -------------------------------------------------------
    // Prevent XSS when injecting user-supplied strings into table cells/labels.
    function escapeHtml(value) {
        if (value === null || value === undefined) return '';
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    // Initials for avatar circle, e.g. "John Doe" -> "JD".
    function initials(name) {
        if (!name) return '?';
        return name.trim().split(/\s+/).slice(0, 2).map(p => p[0].toUpperCase()).join('');
    }

    // --- Confirm modal -----------------------------------------------------
    // Returns a Promise<boolean>. Replaces window.confirm() with a styled modal.
    function confirmDialog(opts) {
        opts = opts || {};
        return new Promise(function (resolve) {
            var modalEl = document.getElementById('confirmModal');
            if (!modalEl) { resolve(window.confirm(opts.message || 'Are you sure?')); return; }

            document.getElementById('confirmModalTitle').textContent = opts.title || 'Are you sure?';
            document.getElementById('confirmModalBody').textContent = opts.message || 'This action cannot be undone.';
            var okBtn = document.getElementById('confirmModalOkBtn');
            okBtn.textContent = opts.okText || 'Delete';
            okBtn.className = 'btn ' + (opts.okClass || 'btn-danger');

            var modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            var resolved = false;
            function onOk() { resolved = true; modal.hide(); }
            function onHidden() {
                okBtn.removeEventListener('click', onOk);
                modalEl.removeEventListener('hidden.bs.modal', onHidden);
                resolve(resolved);
            }
            okBtn.addEventListener('click', onOk);
            modalEl.addEventListener('hidden.bs.modal', onHidden);
            modal.show();
        });
    }

    // --- Inline alert helper ----------------------------------------------
    function showAlert(containerSelector, message, type) {
        var $c = $(containerSelector);
        $c.html(
            '<div class="alert alert-' + (type || 'info') + ' alert-dismissible fade show" role="alert">' +
            escapeHtml(message) +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
            '</div>'
        );
    }

    // --- AJAX auth header --------------------------------------------------
    // Single place to attach the JWT to every jQuery AJAX call.
    function attachAuthHeader() {
        $.ajaxSetup({
            beforeSend: function (xhr) {
                var token = localStorage.getItem('token');
                if (token) xhr.setRequestHeader('Authorization', 'Bearer ' + token);
            }
        });
    }

    // Reflect role in the topbar dropdown badge.
    function updateRoleBadge() {
        var role = localStorage.getItem('userRole');
        if (!role) return;
        document.querySelectorAll('.user-role-badge').forEach(function (el) {
            el.textContent = role.replace(/_/g, ' ');
        });
    }

    document.addEventListener('DOMContentLoaded', updateRoleBadge);

    window.App = {
        escapeHtml: escapeHtml,
        initials: initials,
        confirmDialog: confirmDialog,
        showAlert: showAlert,
        attachAuthHeader: attachAuthHeader
    };
})(window, jQuery);
