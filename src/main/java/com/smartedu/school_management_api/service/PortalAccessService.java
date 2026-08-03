package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.entity.Parent;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.AccessDeniedAppException;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.ParentRepository;
import com.smartedu.school_management_api.repository.StudentGuardianRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.repository.TeachingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The single gate for the self-service portal roles — {@code TEACHER}, {@code STUDENT}
 * and {@code PARENT}.
 *
 * <p>Deliberately separate from {@link SchoolAccessService} rather than folded into it.
 * That service answers "may this admin touch this school", and every one of its entry
 * points calls {@code requireAcademicManager}, which denies all three roles here. This
 * service answers a different question — "which rows are this caller's own" — so keeping
 * them apart leaves the admin tenant rules auditable in one place and the portal scope
 * rules in another.
 *
 * <p>Scope is always derived from the caller's login, never from a request parameter:
 * <ul>
 *   <li>{@code TEACHER} — the classes they are assigned to teach, plus any class they are
 *       homeroom teacher of.</li>
 *   <li>{@code PARENT} — the children linked through {@code parent_students}.</li>
 *   <li>{@code STUDENT} — themselves.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PortalAccessService {

    private final SchoolAccessService access;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final ClassroomRepository classroomRepository;
    private final StudentGuardianRepository studentGuardianRepository;

    /**
     * The authenticated user. Delegates to {@link SchoolAccessService#currentUser()},
     * which is the one method there that does not gate on role.
     */
    @Transactional(readOnly = true)
    public User currentUser() {
        return access.currentUser();
    }

    // --- identity resolution ------------------------------------------------
    // A login and its domain record are separate rows (Teacher.userAccount and friends),
    // so a provisioned account may not be linked yet. That is a setup mistake rather than
    // a permission problem, so it surfaces as a 400 naming the fix.

    @Transactional(readOnly = true)
    public Teacher currentTeacher() {
        User user = currentUser();
        requireRole(user, UserRole.TEACHER);
        return teacherRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Your login is not linked to a staff record yet. Ask an administrator to "
                                + "open your teacher record and set its user account."));
    }

    @Transactional(readOnly = true)
    public Parent currentParent() {
        User user = currentUser();
        requireRole(user, UserRole.PARENT);
        return parentRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Your login is not linked to a guardian record yet. Ask the school office to "
                                + "open your parent record and set its user account."));
    }

    @Transactional(readOnly = true)
    public Student currentStudent() {
        User user = currentUser();
        requireRole(user, UserRole.STUDENT);
        return studentRepository.findByUserAccountId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "Your login is not linked to a student record yet. Ask the school office to "
                                + "open your student record and set its user account."));
    }

    /** The teacher record behind the current login, if there is one. */
    @Transactional(readOnly = true)
    public Optional<Teacher> currentTeacherOrEmpty() {
        User user = currentUser();
        if (user.getRole() != UserRole.TEACHER) {
            return Optional.empty();
        }
        return teacherRepository.findByUserAccountId(user.getId());
    }

    // --- scope --------------------------------------------------------------

    /**
     * The classes a teacher may work with: their teaching assignments plus the classes
     * they are homeroom teacher of.
     *
     * <p>Both halves are keyed on the {@code User} id, so a teacher login without a
     * {@link Teacher} record still sees its homeroom classes —
     * {@link com.smartedu.school_management_api.entity.Classroom#getClassTeacher()} points
     * at a user, and it predates teaching assignments. A school that has not created any
     * assignment rows therefore keeps a working portal.
     */
    @Transactional(readOnly = true)
    public List<Long> visibleClassroomIds() {
        User user = currentUser();
        requireRole(user, UserRole.TEACHER);

        Set<Long> ids = new LinkedHashSet<>(
                teachingAssignmentRepository.findClassroomIdsByTeacherUserAccountId(user.getId()));
        ids.addAll(classroomRepository.findIdsByClassTeacherId(user.getId()));
        return List.copyOf(ids);
    }

    /**
     * The students the caller may read, for whichever portal role they hold.
     *
     * <p>Never empty-collection-queries downstream: an empty scope short-circuits, because
     * an {@code in ()} predicate is not portable SQL.
     */
    @Transactional(readOnly = true)
    public List<Long> visibleStudentIds() {
        User user = currentUser();

        return switch (user.getRole()) {
            case TEACHER -> {
                List<Long> classroomIds = visibleClassroomIds();
                yield classroomIds.isEmpty()
                        ? List.of()
                        : studentRepository.findIdsByClassroomIdIn(classroomIds);
            }
            case PARENT -> studentGuardianRepository.findStudentIdsByParentUserAccountId(user.getId());
            case STUDENT -> List.of(currentStudent().getId());
            default -> throw new AccessDeniedAppException(
                    "This view is only available to teachers, students and parents");
        };
    }

    // --- checks -------------------------------------------------------------

    @Transactional(readOnly = true)
    public void requireClassroomVisible(Long classroomId) {
        if (classroomId == null || !visibleClassroomIds().contains(classroomId)) {
            throw new AccessDeniedAppException("You are not assigned to that class");
        }
    }

    @Transactional(readOnly = true)
    public void requireStudentVisible(Long studentId) {
        if (studentId == null || !visibleStudentIds().contains(studentId)) {
            throw new AccessDeniedAppException("You do not have access to that student's records");
        }
    }

    /**
     * The shared write check for classroom-level records — attendance registers and exam
     * marks. An admin is authorised by the existing tenant rule; a teacher only for a
     * class in their own scope. This is the one place the admin and teacher paths meet, so
     * it lives here rather than being duplicated into each service.
     *
     * @param schoolId the school owning the record, for the admin branch
     */
    @Transactional(readOnly = true)
    public void requireClassroomWriteAccess(Long classroomId, Long schoolId) {
        User user = currentUser();

        if (user.getRole() != null && user.getRole().isAcademicManager()) {
            access.requireSchoolAccess(schoolId);
            return;
        }
        if (user.getRole() == UserRole.TEACHER) {
            requireClassroomVisible(classroomId);
            return;
        }
        throw new AccessDeniedAppException("You do not have permission to change these records");
    }

    /**
     * As {@link #requireClassroomWriteAccess} but for a record that may be pinned to a
     * grade instead of a single class — which is what an exam is, since
     * {@link com.smartedu.school_management_api.entity.Exam#getClassroom()} is null for a
     * grade-wide paper.
     *
     * <p>For a grade-wide paper a teacher is allowed when they teach at least one class in
     * that grade. Marking the whole grade is broader than one class, but it is the paper's
     * own scope: there is no narrower roster to offer, and the alternative would be leaving
     * grade-wide marks enterable by admins only.
     */
    @Transactional(readOnly = true)
    public void requireExamRecordAccess(Long classroomId, Long gradeId, Long schoolId) {
        User user = currentUser();

        if (user.getRole() != null && user.getRole().isAcademicManager()) {
            access.requireSchoolAccess(schoolId);
            return;
        }
        if (user.getRole() != UserRole.TEACHER) {
            throw new AccessDeniedAppException("You do not have permission to change these records");
        }

        List<Long> visible = visibleClassroomIds();
        if (classroomId != null) {
            if (!visible.contains(classroomId)) {
                throw new AccessDeniedAppException("You are not assigned to that class");
            }
            return;
        }

        boolean teachesGrade = gradeId != null
                && !visible.isEmpty()
                && classroomRepository.findByIdInOrderByNameAsc(visible).stream()
                .anyMatch(classroom -> classroom.getGrade() != null
                        && classroom.getGrade().getId().equals(gradeId));

        if (!teachesGrade) {
            throw new AccessDeniedAppException("You do not teach that grade");
        }
    }

    /** True when the caller holds one of the three portal roles. */
    @Transactional(readOnly = true)
    public boolean isPortalUser() {
        UserRole role = currentUser().getRole();
        return role == UserRole.TEACHER || role == UserRole.STUDENT || role == UserRole.PARENT;
    }

    private void requireRole(User user, UserRole expected) {
        if (user.getRole() != expected) {
            throw new AccessDeniedAppException(
                    "This view is only available to " + expected.getLabel().toLowerCase() + " accounts");
        }
    }
}
