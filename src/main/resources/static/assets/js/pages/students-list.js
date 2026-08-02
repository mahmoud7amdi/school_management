/**
 * Students list: server-side search, filters and paging.
 *
 * Filtering happens on the server so the page count stays correct on large
 * rosters, rather than hiding rows from a single fetched page.
 */
(function () {
    'use strict';

    const PAGE_SIZE = 10;
    const COLSPAN = 7;

    const tbody = document.getElementById('studentsTableBody');
    const searchInput = document.getElementById('studentSearch');
    const statusFilter = document.getElementById('filterStatus');
    const gradeFilter = document.getElementById('filterGrade');
    const classroomFilter = document.getElementById('filterClassroom');
    const footer = document.getElementById('tableFooter');

    let page = 0;
    let debounceTimer = null;

    const STATUS_CLASSES = {
        ACTIVE: 'bg-success-subtle text-success-emphasis',
        INACTIVE: 'bg-secondary-subtle text-secondary-emphasis',
        GRADUATED: 'bg-primary-subtle text-primary-emphasis',
        TRANSFERRED: 'bg-info-subtle text-info-emphasis',
        SUSPENDED: 'bg-danger-subtle text-danger-emphasis'
    };

    function statusBadge(student) {
        return UI.badge(student.statusLabel || student.status, STATUS_CLASSES[student.status]);
    }

    function rowHtml(student) {
        const name = UI.escapeHtml(student.fullName);
        const guardian = student.guardianName
            ? UI.escapeHtml(student.guardianName) +
              (student.guardianPhone ? '<div class="small text-secondary">' +
                  UI.escapeHtml(student.guardianPhone) + '</div>' : '')
            : '<span class="text-secondary">&mdash;</span>';

        return '<tr>' +
            '<td><span class="fw-semibold">' + UI.escapeHtml(student.admissionNumber) + '</span></td>' +
            '<td>' +
                '<div class="d-flex align-items-center gap-2">' +
                    '<span class="avatar-initial">' + UI.escapeHtml(UI.initials(student.fullName)) + '</span>' +
                    '<div>' +
                        '<div class="fw-semibold">' + name + '</div>' +
                        '<div class="small text-secondary">' +
                            UI.escapeHtml(student.email || (student.age ? student.age + ' yrs' : '')) +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</td>' +
            '<td>' + UI.dash(student.grade ? student.grade.name : null) + '</td>' +
            '<td>' + UI.dash(student.classroom ? student.classroom.name : null) + '</td>' +
            '<td>' + guardian + '</td>' +
            '<td>' + statusBadge(student) + '</td>' +
            '<td class="text-end row-actions">' +
                '<button type="button" class="btn btn-sm btn-ghost-secondary" data-action="view" ' +
                    'data-id="' + student.id + '" title="View details"><i class="ti ti-eye fs-5"></i></button>' +
                '<a class="btn btn-sm btn-ghost-primary" href="/dashboard/students/add?id=' + student.id + '" ' +
                    'title="Edit"><i class="ti ti-pencil fs-5"></i></a>' +
                '<button type="button" class="btn btn-sm btn-ghost-danger" data-action="delete" ' +
                    'data-id="' + student.id + '" data-name="' + name + '" title="Delete">' +
                    '<i class="ti ti-trash fs-5"></i></button>' +
            '</td>' +
        '</tr>';
    }

    function updateFooter(result) {
        const summary = document.getElementById('resultSummary');
        if (summary) {
            summary.textContent = result.totalElements === 0
                ? 'No students match these filters.'
                : result.totalElements + ' student' + (result.totalElements === 1 ? '' : 's') + ' found';
        }

        if (result.totalPages <= 1) {
            footer.hidden = true;
            return;
        }
        footer.hidden = false;
        document.getElementById('pageInfo').textContent =
            'Page ' + (result.page + 1) + ' of ' + result.totalPages;
        document.getElementById('prevPageItem').classList.toggle('disabled', result.first);
        document.getElementById('nextPageItem').classList.toggle('disabled', result.last);
    }

    async function load() {
        UI.table.loading(tbody, COLSPAN, 'Loading students...');
        const query = Api.query({
            search: searchInput.value.trim(),
            status: statusFilter.value,
            gradeId: gradeFilter.value,
            classroomId: classroomFilter.value,
            page: page,
            size: PAGE_SIZE
        });

        try {
            const result = await Api.get('/api/v1/students' + query);
            if (!result.content.length) {
                UI.table.empty(tbody, COLSPAN,
                    page > 0 ? 'No students on this page.' : 'No students enrolled yet.', 'ti-school-off');
            } else {
                tbody.innerHTML = result.content.map(rowHtml).join('');
            }
            updateFooter(result);
        } catch (error) {
            UI.table.error(tbody, COLSPAN, error.message || 'Failed to load students.');
            footer.hidden = true;
        }
    }

    /** Any filter change resets to the first page, else you can land past the end. */
    function reload() {
        page = 0;
        load();
    }

    async function loadFilterOptions() {
        try {
            const [grades, classrooms] = await Promise.all([
                Api.get('/api/v1/grades'),
                Api.get('/api/v1/classrooms')
            ]);
            UI.fillSelect(gradeFilter, grades, {placeholder: 'All grades'});
            UI.fillSelect(classroomFilter, classrooms, {placeholder: 'All classrooms'});
        } catch (error) {
            UI.toast('Could not load filter options', 'warning');
        }
    }

    async function showDetail(id) {
        const modalEl = document.getElementById('studentDetailModal');
        const body = document.getElementById('studentDetailBody');
        body.innerHTML = '<p class="text-secondary mb-0">Loading...</p>';
        bootstrap.Modal.getOrCreateInstance(modalEl).show();

        try {
            const student = await Api.get('/api/v1/students/' + id);
            document.getElementById('detailEditLink').href = '/dashboard/students/add?id=' + student.id;

            const rows = [
                ['Admission number', student.admissionNumber],
                ['Full name', student.fullName],
                ['Gender', student.gender === 'MALE' ? 'Male' : 'Female'],
                ['Date of birth', UI.formatDate(student.dateOfBirth) +
                    (student.age ? ' (' + student.age + ' years)' : '')],
                ['Email', student.email],
                ['Phone', student.phoneNumber],
                ['Address', student.address],
                ['Grade', student.grade ? student.grade.name : null],
                ['Classroom', student.classroom ? student.classroom.name : null],
                ['School', student.school ? student.school.name : null],
                ['Enrolled on', UI.formatDate(student.enrollmentDate)],
                ['Status', student.statusLabel],
                ['Guardian', student.guardianName],
                ['Guardian phone', student.guardianPhone],
                ['Guardian email', student.guardianEmail]
            ];

            body.innerHTML = '<dl class="row mb-0">' + rows.map(function (pair) {
                return '<dt class="col-sm-4 text-secondary fw-normal">' + UI.escapeHtml(pair[0]) + '</dt>' +
                    '<dd class="col-sm-8 fw-semibold">' + UI.dash(pair[1]) + '</dd>';
            }).join('') + '</dl>';
        } catch (error) {
            body.innerHTML = '<p class="text-danger mb-0">' +
                UI.escapeHtml(error.message || 'Failed to load student.') + '</p>';
        }
    }

    async function remove(id, name) {
        const confirmed = await UI.confirmDialog({
            title: 'Delete student?',
            message: 'This permanently removes ' + name + ' and their enrolment record.',
            okText: 'Delete'
        });
        if (!confirmed) {
            return;
        }
        try {
            await Api.del('/api/v1/students/' + id);
            UI.toast('Student deleted', 'success');
            load();
        } catch (error) {
            UI.toast(error.message || 'Could not delete the student', 'danger');
        }
    }

    // Event delegation: rows are replaced on every load, so bind once on the body.
    tbody.addEventListener('click', function (event) {
        const button = event.target.closest('[data-action]');
        if (!button) {
            return;
        }
        const id = button.getAttribute('data-id');
        if (button.getAttribute('data-action') === 'view') {
            showDetail(id);
        } else {
            remove(id, button.getAttribute('data-name') || 'this student');
        }
    });

    searchInput.addEventListener('input', function () {
        window.clearTimeout(debounceTimer);
        debounceTimer = window.setTimeout(reload, 300);
    });

    [statusFilter, gradeFilter, classroomFilter].forEach(function (select) {
        select.addEventListener('change', reload);
    });

    document.getElementById('resetFilters').addEventListener('click', function () {
        searchInput.value = '';
        statusFilter.value = '';
        gradeFilter.value = '';
        classroomFilter.value = '';
        reload();
    });

    document.getElementById('prevPage').addEventListener('click', function () {
        if (page > 0) {
            page--;
            load();
        }
    });

    document.getElementById('nextPage').addEventListener('click', function () {
        page++;
        load();
    });

    Shell.requireManager()
        .then(function () {
            loadFilterOptions();
            return load();
        })
        .catch(function () { /* redirect already under way */ });
})();
