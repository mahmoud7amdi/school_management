/**
 * Parent portal: a card per child, each linking into the per-child record pages.
 *
 * Clicking through stores the child id under the key portal-shared.js reads, so the
 * destination page opens already showing that child.
 */
(function () {
    'use strict';

    const container = document.getElementById('childCards');
    const summary = document.getElementById('resultSummary');

    function cardHtml(child) {
        const initials = UI.initials(child.fullName);
        return '<div class="col-md-6 col-xl-4">' +
            '<div class="card h-100 shadow-sm">' +
            '<div class="card-body">' +
            '<div class="d-flex align-items-center gap-3 mb-3">' +
            '<span class="avatar avatar-md rounded-circle bg-primary-subtle text-primary-emphasis ' +
            'd-inline-flex align-items-center justify-content-center fw-semibold">' +
            UI.escapeHtml(initials) + '</span>' +
            '<div class="overflow-hidden">' +
            '<h5 class="mb-0 text-truncate">' + UI.escapeHtml(child.fullName || '') + '</h5>' +
            '<p class="mb-0 small text-secondary">' + UI.dash(child.admissionNumber) + '</p>' +
            '</div>' +
            '</div>' +
            '<ul class="list-unstyled small mb-3">' +
            '<li class="d-flex justify-content-between py-1">' +
            '<span class="text-secondary">Grade</span>' +
            '<span class="fw-semibold">' + UI.dash(child.grade ? child.grade.name : null) + '</span></li>' +
            '<li class="d-flex justify-content-between py-1">' +
            '<span class="text-secondary">Class</span>' +
            '<span class="fw-semibold">' + UI.dash(child.classroom ? child.classroom.name : null) + '</span></li>' +
            '<li class="d-flex justify-content-between py-1">' +
            '<span class="text-secondary">Status</span>' +
            '<span>' + UI.badge(child.statusLabel || '',
                child.status === 'ACTIVE'
                    ? 'bg-success-subtle text-success-emphasis'
                    : 'bg-secondary-subtle text-secondary-emphasis') + '</span></li>' +
            '</ul>' +
            '<div class="d-flex flex-wrap gap-2">' +
            '<button type="button" class="btn btn-sm btn-outline-primary" data-child="' + child.id +
            '" data-target="/dashboard/portal/my-attendance">' +
            '<i class="ti ti-calendar-check me-1"></i>Attendance</button>' +
            '<button type="button" class="btn btn-sm btn-outline-primary" data-child="' + child.id +
            '" data-target="/dashboard/portal/my-results">' +
            '<i class="ti ti-file-certificate me-1"></i>Results</button>' +
            '<button type="button" class="btn btn-sm btn-outline-primary" data-child="' + child.id +
            '" data-target="/dashboard/portal/my-fees">' +
            '<i class="ti ti-cash me-1"></i>Fees</button>' +
            '</div>' +
            '</div>' +
            '</div>' +
            '</div>';
    }

    function load() {
        return Api.get('/api/v1/portal/parent/children')
            .then(function (children) {
                children = children || [];

                if (children.length === 0) {
                    summary.textContent = 'No children linked yet.';
                    container.innerHTML =
                        '<div class="col-12"><div class="card shadow-sm"><div class="card-body text-center py-5">' +
                        '<i class="ti ti-users d-block fs-1 opacity-50 mb-2"></i>' +
                        '<p class="text-secondary mb-0">No children are linked to your account yet. ' +
                        'Contact the school office to have them added.</p>' +
                        '</div></div></div>';
                    return;
                }

                summary.textContent = children.length + ' child' + (children.length === 1 ? '' : 'ren');
                container.innerHTML = children.map(cardHtml).join('');
            })
            .catch(function (error) {
                summary.textContent = 'Could not load your children.';
                container.innerHTML = '<div class="col-12"><p class="text-danger mb-0">' +
                    UI.escapeHtml(error.message || 'Failed to load.') + '</p></div>';
            });
    }

    container.addEventListener('click', function (event) {
        const button = event.target.closest('[data-child]');
        if (!button) {
            return;
        }
        // Hand the choice to the destination page rather than passing it in the URL,
        // so the selector there and this card stay in agreement.
        window.localStorage.setItem(Portal.CHILD_KEY, button.getAttribute('data-child'));
        window.location.assign(button.getAttribute('data-target'));
    });

    Shell.requireRole('PARENT')
        .then(load)
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load your children', 'danger');
            }
        });
})();
