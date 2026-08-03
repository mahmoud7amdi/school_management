/**
 * Dashboard shell: authentication guard, topbar identity, role gating, logout.
 *
 * Every page includes this. It exposes `Shell.ready`, a promise resolving to the
 * signed-in user, so page modules can wait for identity before they render:
 *
 *   Shell.ready.then(function (user) { ... });
 */
(function (window) {
    'use strict';

    const USER_KEY = 'sm.user';
    const MANAGER_ROLES = ['SUPER_ADMIN', 'SCHOOL_ADMIN'];
    const PORTAL_ROLES = ['TEACHER', 'STUDENT', 'PARENT'];

    let currentUser = null;

    function cachedUser() {
        try {
            const raw = localStorage.getItem(USER_KEY);
            return raw ? JSON.parse(raw) : null;
        } catch (error) {
            return null;
        }
    }

    function cacheUser(user) {
        if (user) {
            localStorage.setItem(USER_KEY, JSON.stringify(user));
        } else {
            localStorage.removeItem(USER_KEY);
        }
    }

    // --- topbar ------------------------------------------------------------
    function paintUser(user) {
        if (!user) {
            return;
        }

        document.querySelectorAll('.user-full-name').forEach(function (el) {
            el.textContent = user.fullName || user.username || '';
        });
        document.querySelectorAll('.user-username').forEach(function (el) {
            el.textContent = user.username ? '@' + user.username : '';
        });
        document.querySelectorAll('.user-role-badge').forEach(function (el) {
            el.textContent = user.roleLabel || (user.role || '').replace(/_/g, ' ');
        });
        document.querySelectorAll('.school-name').forEach(function (el) {
            el.textContent = user.school ? user.school.name : 'No school assigned';
        });

        if (user.avatarUrl) {
            document.querySelectorAll('.user-avatar').forEach(function (img) {
                img.src = user.avatarUrl;
            });
        }

        const greeting = document.querySelector('.welcome-header');
        if (greeting) {
            const name = (user.fullName || user.username || '').split(' ')[0];
            greeting.textContent = name ? 'Hello ' + name + ',' : 'Hello,';
        }
    }

    // --- role gating -------------------------------------------------------
    /**
     * Reveals elements whose `data-requires-role` list contains the user's role.
     *
     * app.css hides `[data-requires-role]` up front so a restricted item never
     * flashes before this runs. The attribute is removed rather than overridden
     * with an inline style, which lets each element fall back to whatever
     * display the stylesheet intends (flex for nav items, block for cards).
     */
    function applyRoleVisibility(role) {
        document.querySelectorAll('[data-requires-role]').forEach(function (el) {
            const allowed = (el.getAttribute('data-requires-role') || '')
                .split(',')
                .map((value) => value.trim())
                .filter(Boolean);

            if (allowed.includes(role)) {
                el.removeAttribute('data-requires-role');
            } else {
                el.remove();
            }
        });
    }

    function isManager(user) {
        return !!user && MANAGER_ROLES.includes(user.role);
    }

    function isPortal(user) {
        return !!user && PORTAL_ROLES.includes(user.role);
    }

    /**
     * Page-level guard. Sends the user home when their role is not permitted.
     * The dashboard home never calls this, so there is no redirect loop.
     */
    function requireRole() {
        const allowed = Array.prototype.slice.call(arguments).flat();
        return Shell.ready.then(function (user) {
            if (!user || !allowed.includes(user.role)) {
                UI.toast('You do not have access to that page.', 'warning');
                window.setTimeout(function () {
                    window.location.replace('/dashboard/index');
                }, 800);
                return Promise.reject(new Error('Insufficient role'));
            }
            return user;
        });
    }

    // --- logout ------------------------------------------------------------
    async function logout() {
        try {
            // Best effort: revoke server-side, but always clear local state.
            await Api.post('/api/v1/auth/logout', null, {redirectOnUnauthorized: false});
        } catch (error) {
            /* ignored — the token may already be expired or revoked */
        }
        localStorage.removeItem(Api.TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
        window.location.replace('/login');
    }

    function bindLogout() {
        document.querySelectorAll('.logout-btn').forEach(function (btn) {
            btn.addEventListener('click', function (event) {
                event.preventDefault();
                logout();
            });
        });
    }

    // --- sidebar active state ---------------------------------------------
    /** Marks the nav link matching the current path, and opens its dropdown. */
    function markActiveNav() {
        const path = window.location.pathname;
        document.querySelectorAll('#miniSidebar a[href], .offcanvasNav a[href]').forEach(function (link) {
            if (link.getAttribute('href') !== path) {
                return;
            }
            link.classList.add('active');

            const menu = link.closest('.dropdown-menu');
            if (!menu) {
                return;
            }
            const toggle = menu.parentElement
                && menu.parentElement.querySelector('.dropdown-toggle');
            if (toggle) {
                toggle.classList.add('active');
                toggle.setAttribute('aria-expanded', 'true');
                menu.classList.add('show');
            }
        });
    }

    // --- bootstrap ---------------------------------------------------------
    const ready = (async function start() {
        if (!localStorage.getItem(Api.TOKEN_KEY)) {
            Api.redirectToLogin();
            return null;
        }

        // Paint from cache first so the topbar is not empty during the fetch,
        // then reconcile with the server (which also validates the token).
        const cached = cachedUser();
        if (cached) {
            currentUser = cached;
            onDomReady(function () {
                paintUser(cached);
                applyRoleVisibility(cached.role);
            });
        }

        try {
            const user = await Api.get('/api/v1/auth/me');
            currentUser = user;
            cacheUser(user);
            onDomReady(function () {
                paintUser(user);
                if (!cached || cached.role !== user.role) {
                    applyRoleVisibility(user.role);
                }
            });
            return user;
        } catch (error) {
            // A 401 already redirected inside Api; anything else is reported.
            if (!(error instanceof Api.ApiError) || error.status !== 401) {
                onDomReady(() => UI.toast(error.message || 'Could not load your profile', 'danger'));
            }
            return null;
        }
    })();

    function onDomReady(callback) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', callback, {once: true});
        } else {
            callback();
        }
    }

    /**
     * Opt-in Bootstrap plugins. Kept here because the theme's main.js (which used
     * to do this) also bound a competing form-validation handler and targeted demo
     * markup that does not exist in this app.
     */
    function initPlugins() {
        document.querySelectorAll('[data-bs-toggle="tooltip"]')
            .forEach((el) => bootstrap.Tooltip.getOrCreateInstance(el));
        document.querySelectorAll('[data-bs-toggle="popover"]')
            .forEach((el) => bootstrap.Popover.getOrCreateInstance(el));
    }

    onDomReady(function () {
        bindLogout();
        markActiveNav();
        initPlugins();
    });

    const Shell = {
        ready: ready,
        user: () => currentUser,
        isManager: () => isManager(currentUser),
        isSuperAdmin: () => !!currentUser && currentUser.role === 'SUPER_ADMIN',
        isSchoolAdmin: () => !!currentUser && currentUser.role === 'SCHOOL_ADMIN',
        isTeacher: () => !!currentUser && currentUser.role === 'TEACHER',
        isStudent: () => !!currentUser && currentUser.role === 'STUDENT',
        isParent: () => !!currentUser && currentUser.role === 'PARENT',
        isPortalUser: () => isPortal(currentUser),
        requireRole: requireRole,
        requireManager: () => requireRole(MANAGER_ROLES),
        logout: logout,
        onDomReady: onDomReady
    };

    window.Shell = Shell;
})(window);
