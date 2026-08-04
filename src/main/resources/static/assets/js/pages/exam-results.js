/**
 * Marks entry sheet.
 *
 * Flow: pick an exam, load its marks (a row per student already in the exam's
 * results), enter marks inline, then save the whole grid in one call — the
 * service upserts on (exam, student).
 */
(function () {
    'use strict';

    const GRADE_CLASSES = {
        'A+': 'bg-success-subtle text-success-emphasis',
        A: 'bg-success-subtle text-success-emphasis',
        B: 'bg-info-subtle text-info-emphasis',
        C: 'bg-warning-subtle text-warning-emphasis',
        D: 'bg-warning-subtle text-warning-emphasis',
        F: 'bg-danger-subtle text-danger-emphasis',
        ABS: 'bg-secondary-subtle text-secondary-emphasis'
    };

    let currentExam = null;
    let currentMax = 0;
    let currentPass = 0;
    let allStudents = [];   // for seeding the grid when the exam has no marks yet

    const tbody = document.getElementById('marksBody');

    function gradeBadge(letter) {
        if (!letter) {
            return '<span class="text-secondary">&mdash;</span>';
        }
        return UI.badge(letter, GRADE_CLASSES[letter] || 'bg-secondary-subtle text-secondary-emphasis');
    }

    function updateStats() {
        let total = 0;
        let marked = 0;
        let passed = 0;
        let sum = 0;

        tbody.querySelectorAll('tr[data-student]').forEach(function (rowEl) {
            total++;
            const value = rowEl.querySelector('.marks-input').value;
            const absent = rowEl.querySelector('.absent-check').checked;
            if (absent) {
                return; // absent is not "marked", but counts toward the total
            }
            if (value !== '') {
                const numeric = Number(value);
                marked++;
                sum += numeric;
                if (numeric >= currentPass) {
                    passed++;
                }
            }
        });

        document.getElementById('statTotal').textContent = total;
        document.getElementById('statMarked').textContent = marked;
        document.getElementById('statPassed').textContent = passed;
        document.getElementById('statAverage').textContent =
            marked ? (sum / marked).toFixed(1) : '—';
    }

    function renderRows(results) {
        const rows = (results || []).map(function (result) {
            const name = UI.escapeHtml(result.student ? result.student.name : '—');
            const marks = result.absent ? '' : (result.marksObtained == null ? '' : result.marksObtained);
            return '<tr data-student="' + result.student.id + '">' +
                '<td class="fw-semibold">' + name + '</td>' +
                '<td class="text-secondary">' + UI.escapeHtml(result.admissionNumber || '') + '</td>' +
                '<td><input type="number" class="form-control form-control-sm marks-input" ' +
                    'min="0" max="' + currentMax + '" step="0.01" value="' + marks + '" /></td>' +
                '<td class="text-center"><input type="checkbox" class="form-check-input absent-check" ' +
                    (result.absent ? 'checked' : '') + ' aria-label="Absent" /></td>' +
                '<td class="grade-cell">' + gradeBadge(result.gradeLetter) + '</td>' +
                '<td><input type="text" class="form-control form-control-sm remark-input" maxlength="255" ' +
                    'value="' + UI.escapeHtml(result.remarks || '') + '" placeholder="Optional note" /></td>' +
            '</tr>';
        }).join('');

        tbody.innerHTML = rows || '<tr><td colspan="6" class="table-state">' +
            '<i class="ti ti-users-off"></i>No students match this exam. Check the exam\'s ' +
            'grade and classroom, then enrol students there.</td></tr>';

        updateStats();
        document.getElementById('saveMarksBtn').disabled = !rows;
    }

    /**
     * Rows for a freshly-created exam.
     *
     * The results endpoint only returns marks that already exist, so a new exam
     * would render an empty grid with nothing to type into. Seed it from the
     * students the exam actually applies to: its classroom if it names one,
     * otherwise everyone in its grade.
     */
    function seedRowsFromRoster(exam) {
        return allStudents
            .filter(function (student) {
                if (exam.classroom) {
                    return student.classroom && String(student.classroom.id) === String(exam.classroom.id);
                }
                return student.grade && exam.grade && String(student.grade.id) === String(exam.grade.id);
            })
            .map(function (student) {
                return {
                    student: {id: student.id, name: student.fullName},
                    admissionNumber: student.admissionNumber,
                    marksObtained: null,
                    absent: false,
                    gradeLetter: null,
                    remarks: null
                };
            });
    }

    async function loadSheet() {
        const examId = document.getElementById('examId').value;
        if (!examId) {
            UI.toast('Choose an exam first.', 'warning');
            return;
        }
        UI.table.loading(tbody, 6, 'Loading marks...');
        try {
            currentExam = await Api.get('/api/v1/exams/' + examId);
            currentMax = Number(currentExam.maxMarks);
            currentPass = Number(currentExam.passMarks);
            let results = await Api.get('/api/v1/exams/' + examId + '/results');
            if (!results.length) {
                results = seedRowsFromRoster(currentExam);
            }

            document.getElementById('marksMeta').textContent =
                currentExam.title + ' · ' +
                (currentExam.subject ? currentExam.subject.name : '') + ' · max ' +
                currentExam.maxMarks + ' · pass ' + currentExam.passMarks;
            document.getElementById('statsRow').hidden = false;
            renderRows(results);
        } catch (error) {
            UI.table.error(tbody, 6, error.message || 'Could not load the marks sheet.');
            document.getElementById('statsRow').hidden = true;
        }
    }

    function buildPayload() {
        const entries = [];
        tbody.querySelectorAll('tr[data-student]').forEach(function (rowEl) {
            const value = rowEl.querySelector('.marks-input').value;
            const absent = rowEl.querySelector('.absent-check').checked;
            entries.push({
                studentId: rowEl.getAttribute('data-student'),
                marksObtained: absent ? null : (value === '' ? null : value),
                absent: absent,
                remarks: rowEl.querySelector('.remark-input').value.trim() || null
            });
        });
        return {examId: currentExam.id, entries: entries};
    }

    // Re-derive the letter grade live as marks change.
    function letterFor(marksValue) {
        if (marksValue === '' || marksValue === null || marksValue === undefined) {
            return null;
        }
        const pct = (Number(marksValue) / currentMax) * 100;
        if (pct >= 90) {
            return 'A+';
        }
        if (pct >= 80) {
            return 'A';
        }
        if (pct >= 70) {
            return 'B';
        }
        if (pct >= 60) {
            return 'C';
        }
        if (pct >= 50) {
            return 'D';
        }
        return 'F';
    }

    tbody.addEventListener('input', function (event) {
        const rowEl = event.target.closest('tr[data-student]');
        if (!rowEl) {
            return;
        }
        if (event.target.classList.contains('marks-input')) {
            const cell = rowEl.querySelector('.grade-cell');
            cell.innerHTML = gradeBadge(letterFor(event.target.value));
        }
        updateStats();
    });

    tbody.addEventListener('change', function (event) {
        if (event.target.classList.contains('absent-check')) {
            const rowEl = event.target.closest('tr[data-student]');
            const marksInput = rowEl.querySelector('.marks-input');
            if (event.target.checked) {
                marksInput.value = '';
                marksInput.disabled = true;
            } else {
                marksInput.disabled = false;
            }
            rowEl.querySelector('.grade-cell').innerHTML = gradeBadge(
                event.target.checked ? 'ABS' : null);
            updateStats();
        }
    });

    document.getElementById('marksControls').addEventListener('submit', function (event) {
        event.preventDefault();
        loadSheet();
    });

    document.getElementById('saveMarksBtn').addEventListener('click', async function () {
        const btn = this;
        const restore = UI.busy(btn, 'Saving...');
        try {
            await Api.post('/api/v1/exams/' + currentExam.id + '/results', buildPayload());
            UI.toast('Marks saved', 'success');
            await loadSheet();
        } catch (error) {
            UI.toast(error.message || 'Could not save the marks', 'danger');
        } finally {
            restore();
        }
    });

    /**
     * A teacher's seeding pool: the rosters of the classes they teach, flattened.
     *
     * They cannot call /students, so the grid is seeded from the portal's per-class
     * rosters instead. Duplicates are dropped because a class can appear once per
     * subject taught in it.
     */
    async function loadTeacherStudents() {
        const classes = await Api.get('/api/v1/portal/teacher/classes');
        const rosters = await Promise.all((classes || []).map(function (item) {
            return Api.get('/api/v1/portal/teacher/classes/' + encodeURIComponent(item.id) + '/roster')
                .catch(() => []);
        }));

        const byId = new Map();
        rosters.flat().forEach(function (student) {
            byId.set(String(student.id), student);
        });
        return Array.from(byId.values());
    }

    Shell.requireRole(['SCHOOL_ADMIN', 'TEACHER'])
        .then(async function () {
            // A teacher sees only the papers they may mark; an admin sees the school's.
            // Rosters differ the same way: the portal returns just their own classes.
            const teacherMode = Shell.isTeacher();

            const [exams, students] = await Promise.all([
                Api.get(teacherMode ? '/api/v1/portal/teacher/exams' : '/api/v1/exams'),
                // The unpaged route: /api/v1/students is paged and returns an envelope,
                // which has no .filter() for seedRowsFromRoster below.
                teacherMode ? loadTeacherStudents() : Api.get('/api/v1/students/all')
            ]);
            allStudents = students;

            UI.fillSelect(document.getElementById('examId'), exams, {
                placeholder: 'Select exam',
                label: (exam) => exam.title + ' — ' +
                    (exam.subject ? exam.subject.name : '') + ' · ' +
                    UI.formatDate(exam.examDate)
            });

            // Support arriving from the exams list with ?examId= already set.
            const params = new URLSearchParams(window.location.search);
            const preselected = params.get('examId');
            if (preselected && document.querySelector('#examId option[value="' + preselected + '"]')) {
                document.getElementById('examId').value = preselected;
                loadSheet();
            } else if (!exams.length) {
                UI.toast(teacherMode
                    ? 'There are no exams for your classes yet.'
                    : 'Schedule an exam before entering marks.', 'warning');
            }
        })
        .catch(function (error) {
            if (error && error.message !== 'Insufficient role') {
                UI.toast(error.message || 'Could not load the marks page', 'danger');
            }
        });
})();
