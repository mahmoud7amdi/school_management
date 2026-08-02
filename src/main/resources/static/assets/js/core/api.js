/**
 * Single HTTP entry point for the dashboard.
 *
 * Every backend response uses the same envelope:
 *   { status, success, message, data, error, fieldErrors, timestamp }
 * so this unwraps `data` on success and throws an ApiError carrying `message`
 * plus any per-field messages on failure.
 */
(function (window) {
    'use strict';

    const TOKEN_KEY = 'sm.token';

    class ApiError extends Error {
        constructor(message, status, fieldErrors) {
            super(message);
            this.name = 'ApiError';
            this.status = status;
            this.fieldErrors = fieldErrors || null;
        }
    }

    function token() {
        return localStorage.getItem(TOKEN_KEY);
    }

    /** Sends the caller back to the sign-in page, remembering where they were. */
    function redirectToLogin() {
        const here = window.location.pathname + window.location.search;
        const target = here && here !== '/login' ? '?next=' + encodeURIComponent(here) : '';
        window.location.replace('/login' + target);
    }

    async function request(method, url, body, options) {
        options = options || {};

        const headers = {Accept: 'application/json'};
        const jwt = token();
        if (jwt) {
            headers.Authorization = 'Bearer ' + jwt;
        }
        if (body !== undefined && body !== null) {
            headers['Content-Type'] = 'application/json';
        }

        let response;
        try {
            response = await fetch(url, {
                method: method,
                headers: headers,
                body: body !== undefined && body !== null ? JSON.stringify(body) : undefined
            });
        } catch (networkError) {
            throw new ApiError('Cannot reach the server. Check your connection and try again.', 0, null);
        }

        // 204 and empty bodies are valid; treat an unparseable body as no payload.
        let payload = null;
        const text = await response.text();
        if (text) {
            try {
                payload = JSON.parse(text);
            } catch (parseError) {
                payload = null;
            }
        }

        if (response.status === 401 && options.redirectOnUnauthorized !== false) {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem('sm.user');
            redirectToLogin();
            throw new ApiError('Your session has ended. Please sign in again.', 401, null);
        }

        if (!response.ok) {
            const message = (payload && (payload.message || payload.error))
                || 'Request failed (' + response.status + ')';
            throw new ApiError(message, response.status, payload && payload.fieldErrors);
        }

        // Unwrap the envelope; fall back to the raw payload for non-enveloped routes.
        return payload && Object.prototype.hasOwnProperty.call(payload, 'data')
            ? payload.data
            : payload;
    }

    window.Api = {
        ApiError: ApiError,
        TOKEN_KEY: TOKEN_KEY,
        get: (url, options) => request('GET', url, null, options),
        post: (url, body, options) => request('POST', url, body, options),
        put: (url, body, options) => request('PUT', url, body, options),
        del: (url, options) => request('DELETE', url, null, options),
        redirectToLogin: redirectToLogin,
        /** Builds a query string from an object, dropping null/empty values. */
        query: function (params) {
            const search = new URLSearchParams();
            Object.keys(params || {}).forEach(function (key) {
                const value = params[key];
                if (value !== null && value !== undefined && value !== '') {
                    search.append(key, value);
                }
            });
            const qs = search.toString();
            return qs ? '?' + qs : '';
        }
    };
})(window);
