package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.fee.FeePaymentRequest;
import com.smartedu.school_management_api.dto.fee.FeePaymentResponse;
import com.smartedu.school_management_api.dto.fee.FeeStructureRequest;
import com.smartedu.school_management_api.dto.fee.FeeStructureResponse;
import com.smartedu.school_management_api.dto.fee.StudentFeeLedgerResponse;
import com.smartedu.school_management_api.service.FeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
public class FeeController {

    private final FeeService feeService;

    // --- Catalogue --------------------------------------------------------

    @GetMapping("/structures")
    public ResponseEntity<ApiResponse<List<FeeStructureResponse>>> getAllFeeStructures() {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getAllFeeStructures(), "Fee items loaded"));
    }

    @GetMapping("/structures/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> getFeeStructureById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getFeeStructureById(id), "Fee item loaded"));
    }

    @PostMapping("/structures")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> createFeeStructure(
            @Valid @RequestBody FeeStructureRequest request) {
        FeeStructureResponse created = feeService.createFeeStructure(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Fee item added successfully"));
    }

    @PutMapping("/structures/{id}")
    public ResponseEntity<ApiResponse<FeeStructureResponse>> updateFeeStructure(
            @PathVariable Long id, @Valid @RequestBody FeeStructureRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.updateFeeStructure(id, request),
                "Fee item updated successfully"));
    }

    @DeleteMapping("/structures/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFeeStructure(@PathVariable Long id) {
        feeService.deleteFeeStructure(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Fee item deleted successfully"));
    }

    // --- Payments ---------------------------------------------------------

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<FeePaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getAllPayments(), "Payments loaded"));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<FeePaymentResponse>> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getPaymentById(id), "Payment loaded"));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<FeePaymentResponse>> createPayment(
            @Valid @RequestBody FeePaymentRequest request) {
        FeePaymentResponse created = feeService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Payment recorded successfully"));
    }

    @PutMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<FeePaymentResponse>> updatePayment(
            @PathVariable Long id, @Valid @RequestBody FeePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.updatePayment(id, request),
                "Payment updated successfully"));
    }

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<String>> deletePayment(@PathVariable Long id) {
        feeService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Payment deleted successfully"));
    }

    // --- Student ledger ---------------------------------------------------

    /** What one student owes across every fee item applying to their grade. */
    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<List<StudentFeeLedgerResponse>>> getLedgerForStudent(
            @RequestParam Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(feeService.getLedgerForStudent(studentId),
                "Ledger loaded"));
    }
}
