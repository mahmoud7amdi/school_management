/**
 * Shared wiring for the two enrollment forms.
 *
 * A classroom only belongs to one grade in one academic year, so the picker is
 * narrowed by both whenever either changes — otherwise the form happily offers
 * classrooms the server will reject.
 */
(function (window) {
    'use strict';

    function bindClassroomFilter(config) {
        const gradeSelect = document.getElementById(config.gradeId || 'gradeId');
        const yearSelect = document.getElementById(config.yearId || 'academicYearId');
        const classroomSelect = document.getElementById(config.classroomId || 'classroomId');

        function refresh(selectedId) {
            const gradeValue = gradeSelect.value;
            const yearValue = yearSelect.value;
            const matching = (config.classrooms() || []).filter(function (classroom) {
                const gradeOk = !gradeValue
                    || (classroom.grade && String(classroom.grade.id) === String(gradeValue));
                const yearOk = !yearValue
                    || (classroom.academicYear && String(classroom.academicYear.id) === String(yearValue));
                return gradeOk && yearOk;
            });

            UI.fillSelect(classroomSelect, matching, {
                placeholder: config.placeholder || 'Not placed yet',
                label: (classroom) => classroom.capacity
                    ? classroom.name + ' (' + classroom.studentCount + '/' + classroom.capacity + ')'
                    : classroom.name
            });
            if (selectedId !== undefined) {
                classroomSelect.value = selectedId === null ? '' : String(selectedId);
            }
        }

        gradeSelect.addEventListener('change', () => refresh());
        yearSelect.addEventListener('change', () => refresh());
        return refresh;
    }

    window.EnrollmentForm = {bindClassroomFilter: bindClassroomFilter};
})(window);
