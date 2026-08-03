package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.fee.FeePaymentRequest;
import com.smartedu.school_management_api.dto.fee.FeePaymentResponse;
import com.smartedu.school_management_api.dto.fee.FeeStructureRequest;
import com.smartedu.school_management_api.dto.fee.FeeStructureResponse;
import com.smartedu.school_management_api.dto.fee.StudentFeeLedgerResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.FeePayment;
import com.smartedu.school_management_api.entity.FeeStructure;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.PaymentStatus;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.FeeMapper;
import com.smartedu.school_management_api.repository.AcademicYearRepository;
import com.smartedu.school_management_api.repository.FeePaymentRepository;
import com.smartedu.school_management_api.repository.FeeStructureRepository;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.service.FeeService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final GradeRepository gradeRepository;
    private final AcademicYearRepository academicYearRepository;
    private final StudentRepository studentRepository;
    private final SchoolAccessService access;
    private final FeeMapper mapper;

    // --- Catalogue --------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<FeeStructureResponse> getAllFeeStructures() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<FeeStructure> fees = schoolId == null
                ? feeStructureRepository.findAllByOrderByAcademicYearStartDateDescNameAsc()
                : feeStructureRepository.findBySchoolIdOrderByAcademicYearStartDateDescNameAsc(schoolId);

        return fees.stream()
                .map(fee -> mapper.toResponse(fee,
                        sumCompleted(feePaymentRepository.findByFeeStructureIdAndStatus(
                                fee.getId(), PaymentStatus.COMPLETED)),
                        feePaymentRepository.countByFeeStructureId(fee.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FeeStructureResponse getFeeStructureById(Long id) {
        FeeStructure fee = loadAccessibleStructure(id);
        return mapper.toResponse(fee,
                sumCompleted(feePaymentRepository.findByFeeStructureIdAndStatus(id, PaymentStatus.COMPLETED)),
                feePaymentRepository.countByFeeStructureId(id));
    }

    @Override
    @Transactional
    public FeeStructureResponse createFeeStructure(FeeStructureRequest request) {
        // The grade pins the school, so the year must match it rather than being
        // accepted blindly from a super admin.
        Grade grade = loadAccessibleGrade(request.gradeId());
        Long schoolId = grade.getSchool().getId();
        AcademicYear year = loadAccessibleYear(request.academicYearId(), schoolId);
        String name = request.name().trim();

        if (feeStructureRepository.existsByAcademicYearIdAndGradeIdAndNameIgnoreCase(
                year.getId(), grade.getId(), name)) {
            throw new DuplicateResourceException(
                    "A fee named '" + name + "' already exists for this grade and academic year");
        }

        FeeStructure fee = FeeStructure.builder()
                .name(name)
                .feeType(request.feeType())
                .amount(request.amount())
                .dueDate(request.dueDate())
                .description(trimToNull(request.description()))
                .grade(grade)
                .academicYear(year)
                .school(grade.getSchool())
                .build();

        return mapper.toResponse(feeStructureRepository.save(fee), BigDecimal.ZERO, 0L);
    }

    @Override
    @Transactional
    public FeeStructureResponse updateFeeStructure(Long id, FeeStructureRequest request) {
        FeeStructure fee = loadAccessibleStructure(id);
        Grade grade = loadAccessibleGrade(request.gradeId());
        Long schoolId = grade.getSchool().getId();
        AcademicYear year = loadAccessibleYear(request.academicYearId(), schoolId);
        String name = request.name().trim();

        if (feeStructureRepository.existsByAcademicYearIdAndGradeIdAndNameIgnoreCaseAndIdNot(
                year.getId(), grade.getId(), name, id)) {
            throw new DuplicateResourceException(
                    "A fee named '" + name + "' already exists for this grade and academic year");
        }

        fee.setName(name);
        fee.setFeeType(request.feeType());
        fee.setAmount(request.amount());
        fee.setDueDate(request.dueDate());
        fee.setDescription(trimToNull(request.description()));
        fee.setGrade(grade);
        fee.setAcademicYear(year);
        fee.setSchool(grade.getSchool());

        return mapper.toResponse(feeStructureRepository.save(fee),
                sumCompleted(feePaymentRepository.findByFeeStructureIdAndStatus(id, PaymentStatus.COMPLETED)),
                feePaymentRepository.countByFeeStructureId(id));
    }

    @Override
    @Transactional
    public void deleteFeeStructure(Long id) {
        FeeStructure fee = loadAccessibleStructure(id);

        // Payments are money history; they are never cascaded away.
        long paymentCount = feePaymentRepository.countByFeeStructureId(id);
        if (paymentCount > 0) {
            throw new BadRequestException("This fee item has " + paymentCount
                    + " payment(s) recorded. Archive it instead of deleting it.");
        }
        feeStructureRepository.delete(fee);
    }

    // --- Payments ---------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<FeePaymentResponse> getAllPayments() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<FeePayment> payments = schoolId == null
                ? feePaymentRepository.findAllByOrderByPaymentDateDesc()
                : feePaymentRepository.findBySchoolIdOrderByPaymentDateDesc(schoolId);
        return payments.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FeePaymentResponse getPaymentById(Long id) {
        return mapper.toResponse(loadAccessiblePayment(id));
    }

    @Override
    @Transactional
    public FeePaymentResponse createPayment(FeePaymentRequest request) {
        // The student pins the school; the fee item must sit in the same tenant.
        Student student = loadAccessibleStudent(request.studentId());
        Long schoolId = student.getSchool().getId();

        FeeStructure fee = loadAccessibleStructure(request.feeStructureId());
        if (!fee.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The selected fee item belongs to a different school");
        }
        assertGradeMatches(fee, student);

        String receiptNumber = request.receiptNumber().trim();
        if (feePaymentRepository.existsBySchoolIdAndReceiptNumberIgnoreCase(schoolId, receiptNumber)) {
            throw new DuplicateResourceException(
                    "A payment with receipt number '" + receiptNumber + "' already exists in this school");
        }

        FeePayment payment = FeePayment.builder()
                .receiptNumber(receiptNumber)
                .amountPaid(request.amountPaid())
                .paymentDate(request.paymentDate())
                .method(request.method())
                .status(request.status() == null ? PaymentStatus.COMPLETED : request.status())
                .remarks(trimToNull(request.remarks()))
                .student(student)
                .feeStructure(fee)
                .school(student.getSchool())
                .build();

        return mapper.toResponse(feePaymentRepository.save(payment));
    }

    @Override
    @Transactional
    public FeePaymentResponse updatePayment(Long id, FeePaymentRequest request) {
        FeePayment payment = loadAccessiblePayment(id);
        Student student = loadAccessibleStudent(request.studentId());
        Long schoolId = student.getSchool().getId();

        FeeStructure fee = loadAccessibleStructure(request.feeStructureId());
        if (!fee.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The selected fee item belongs to a different school");
        }
        assertGradeMatches(fee, student);

        String receiptNumber = request.receiptNumber().trim();
        if (feePaymentRepository.existsBySchoolIdAndReceiptNumberIgnoreCaseAndIdNot(
                schoolId, receiptNumber, id)) {
            throw new DuplicateResourceException(
                    "A payment with receipt number '" + receiptNumber + "' already exists in this school");
        }

        payment.setReceiptNumber(receiptNumber);
        payment.setAmountPaid(request.amountPaid());
        payment.setPaymentDate(request.paymentDate());
        payment.setMethod(request.method());
        if (request.status() != null) {
            payment.setStatus(request.status());
        }
        payment.setRemarks(trimToNull(request.remarks()));
        payment.setStudent(student);
        payment.setFeeStructure(fee);
        payment.setSchool(student.getSchool());

        return mapper.toResponse(feePaymentRepository.save(payment));
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        feePaymentRepository.delete(loadAccessiblePayment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeeLedgerResponse> getLedgerForStudent(Long studentId) {
        Student student = loadAccessibleStudent(studentId);
        Long schoolId = student.getSchool().getId();

        // Every fee item that applies to the student's grade, from any open year.
        List<FeeStructure> structures = feeStructureRepository.findByGradeIdOrderByNameAsc(student.getGrade().getId());
        return structures.stream()
                .filter(fee -> fee.getSchool().getId().equals(schoolId))
                .map(fee -> mapper.toLedger(student, fee,
                        sumCompleted(feePaymentRepository.findByStudentIdAndFeeStructureIdAndStatusOrderByPaymentDateAsc(
                                student.getId(), fee.getId(), PaymentStatus.COMPLETED))))
                .toList();
    }

    // ------------------------------------------------------------------ helpers

    private FeeStructure loadAccessibleStructure(Long id) {
        FeeStructure fee = feeStructureRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Fee item", id));
        access.requireSchoolAccess(fee.getSchool().getId());
        return fee;
    }

    private FeePayment loadAccessiblePayment(Long id) {
        FeePayment payment = feePaymentRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Fee payment", id));
        access.requireSchoolAccess(payment.getSchool().getId());
        return payment;
    }

    private Grade loadAccessibleGrade(Long gradeId) {
        Grade grade = gradeRepository.findWithSchoolById(gradeId)
                .orElseThrow(() -> NotFoundException.of("Grade", gradeId));
        access.requireSchoolAccess(grade.getSchool().getId());
        return grade;
    }

    private AcademicYear loadAccessibleYear(Long yearId, Long schoolId) {
        AcademicYear year = academicYearRepository.findWithSchoolById(yearId)
                .orElseThrow(() -> NotFoundException.of("Academic year", yearId));
        if (!year.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The academic year belongs to a different school");
        }
        return year;
    }

    private Student loadAccessibleStudent(Long studentId) {
        Student student = studentRepository.findWithRelationsById(studentId)
                .orElseThrow(() -> NotFoundException.of("Student", studentId));
        access.requireSchoolAccess(student.getSchool().getId());
        return student;
    }

    /** A fee item and a student must line up on grade, or the ledger would be meaningless. */
    private void assertGradeMatches(FeeStructure fee, Student student) {
        if (student.getGrade() == null || fee.getGrade() == null
                || !student.getGrade().getId().equals(fee.getGrade().getId())) {
            throw new BadRequestException("This fee item does not apply to the student's grade");
        }
    }

    private BigDecimal sumCompleted(List<FeePayment> payments) {
        return payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .map(FeePayment::getAmountPaid)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
