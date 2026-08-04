/**
 * Public About Us page.
 *
 * Plain DOM work with no Shell or UI: those belong to the signed-in dashboard, and this
 * page is read by visitors with no session. Everything is written with textContent, so
 * content typed by an administrator is never treated as markup.
 */
(function () {
    'use strict';

    function setText(id, value) {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = value || '';
        }
    }

    /** Fills a contact row and reveals it; leaves it hidden when there is no value. */
    function setRow(rowId, valueId, value, href) {
        const row = document.getElementById(rowId);
        const target = document.getElementById(valueId);
        if (!row || !target || !value) {
            return false;
        }
        target.textContent = value;
        if (href) {
            target.setAttribute('href', href);
        }
        row.classList.remove('d-none');
        return true;
    }

    function render(about) {
        document.title = (about.title || 'About Us') + ' | School Management';
        setText('aboutTitle', about.title);
        setText('aboutTagline', about.tagline);
        setText('aboutBody', about.body);

        if (about.mission) {
            setText('aboutMission', about.mission);
            document.getElementById('aboutMissionCard').classList.remove('d-none');
        }

        // Only trust http(s) for the website link: an administrator could otherwise
        // save a javascript: URL that would run when a visitor clicked it.
        const safeWebsite = about.website && /^https?:\/\//i.test(about.website)
            ? about.website
            : null;

        const shown = [
            setRow('aboutEmailRow', 'aboutEmail', about.contactEmail,
                about.contactEmail ? 'mailto:' + about.contactEmail : null),
            setRow('aboutPhoneRow', 'aboutPhone', about.contactPhone),
            setRow('aboutAddressRow', 'aboutAddress', about.address),
            setRow('aboutWebsiteRow', 'aboutWebsite', safeWebsite, safeWebsite)
        ].some(Boolean);

        if (shown) {
            document.getElementById('aboutContactCard').classList.remove('d-none');
        }

        if (about.lastUpdated) {
            const updated = document.getElementById('aboutUpdated');
            updated.textContent = 'Last updated ' + about.lastUpdated;
            updated.classList.remove('d-none');
        }
    }

    Api.get('/api/v1/about', {redirectOnUnauthorized: false})
        .then(render)
        .catch(function () {
            setText('aboutTitle', 'About Us');
            setText('aboutBody', 'This page is unavailable right now. Please try again later.');
        });
})();
