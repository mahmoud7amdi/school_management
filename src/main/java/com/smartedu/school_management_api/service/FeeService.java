package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.fee.FeePaymentRequest;
import com.smartedu.school_management_api.dto.fee.FeePaymentResponse;
import com.smartedu.school_management_api.dto.fee.FeeStructureRequest;
import com.smartedu.school_management_api.dto.fee.FeeStructureResponse;
import com.smartedu.school_management_api.dto.fee.StudentFeeLedgerResponse;

import java.util.List;

public interface FeeService {

    // --- Catalogue --------------------------------------------------------
    List<FeeStructureResponse> getAllFeeStructures();

    FeeStructureResponse getFeeStructureById(Long id);

    FeeStructureResponse createFeeStructure(FeeStructureRequest request);

    FeeStructureResponse updateFeeStructure(Long id, FeeStructureRequest request);

    void deleteFeeStructure(Long id);

    // --- Payments ---------------------------------------------------------
    List<FeePaymentResponse> getAllPayments();

    FeePaymentResponse getPaymentById(Long id);

    FeePaymentResponse createPayment(FeePaymentRequest request);

    FeePaymentResponse updatePayment(Long id, FeePaymentRequest request);

    void deletePayment(Long id);

    /** What one student owes across every fee item applying to their grade. */
    List<StudentFeeLedgerResponse> getLedgerForStudent(Long studentId);
}
