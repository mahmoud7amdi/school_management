/**
 * Shared plumbing for the student/parent record pages.
 *
 * The three pages (attendance, results, fees) differ only in what they render: each one
 * reads the same student, chosen the same way. A student has exactly one — themselves, and
 * the server ignores any id they send. A parent picks from their children, so these pages
 * show a selector and remember the choice for the session.
 */
(function (window) {
    'use strict';

    const CHILD_KEY = 'sm.portal.childId';

    /**
     * Prepares the student context for a page.
     *
     * @param {object} options {picker, onChange}
     *   picker   — optional <select> to fill for a parent
     *   onChange — called with the chosen studentId (null for a student's own view)
     * @returns {Promise<{studentId: (number|null), children: Array}>}
     */
    function initStudentContext(options) {
        options = options || {};
        const picker = options.picker;
        const wrapper = options.pickerWrapper;

        return Shell.ready.then(function (user) {
            if (!user) {
                return {studentId: null, children: []};
            }

            // A student reads their own record; no id needed or accepted.
            if (Shell.isStudent()) {
                if (wrapper) {
                    wrapper.remove();
                }
                return {studentId: null, children: []};
            }

            if (!Shell.isParent()) {
                return {studentId: null, children: []};
            }

            return Api.get('/api/v1/portal/parent/children').then(function (children) {
                if (!children || children.length === 0) {
                    if (wrapper) {
                        wrapper.remove();
                    }
                    return {studentId: null, children: []};
                }

                // One child needs no selector — showing a single-option dropdown is noise.
                if (children.length === 1 && wrapper) {
                    wrapper.remove();
                }

                if (picker && children.length > 1) {
                    UI.fillSelect(picker, children, {
                        value: (child) => child.id,
                        label: (child) => child.fullName +
                            (child.classroom ? ' — ' + child.classroom.name : ''),
                        placeholder: null
                    });

                    const remembered = window.localStorage.getItem(CHILD_KEY);
                    const known = children.some((child) => String(child.id) === remembered);
                    picker.value = known ? remembered : String(children[0].id);

                    picker.addEventListener('change', function () {
                        window.localStorage.setItem(CHILD_KEY, picker.value);
                        if (options.onChange) {
                            options.onChange(Number(picker.value));
                        }
                    });

                    return {studentId: Number(picker.value), children: children};
                }

                return {studentId: children[0].id, children: children};
            });
        });
    }

    /** Appends ?studentId= only when there is one, so a student's call stays clean. */
    function scopedUrl(path, studentId) {
        return path + (studentId ? Api.query({studentId: studentId}) : '');
    }

    /** Guard for pages both families use. */
    function requireFamilyRole() {
        return Shell.requireRole(['STUDENT', 'PARENT']);
    }

    window.Portal = {
        initStudentContext: initStudentContext,
        scopedUrl: scopedUrl,
        requireFamilyRole: requireFamilyRole,
        CHILD_KEY: CHILD_KEY
    };
})(window);
