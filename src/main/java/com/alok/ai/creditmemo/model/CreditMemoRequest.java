package com.alok.ai.creditmemo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request model for credit memo generation
 */
public record CreditMemoRequest(
    // Requester Information
    @NotNull(message = "Requester information is required")
    @Valid
    RequesterInfo requester,
    
    // Issuer Information (for BUSINESS_CUSTOMER: the business issuing the credit memo)
    @Valid
    IssuerInfo issuer,
    
    // Customer Information (Recipient of the credit memo)
    @NotNull(message = "Customer information is required")
    @Valid
    CustomerInfo customer,
    
    // Original Transaction Details
    @NotNull(message = "Original transaction information is required")
    @Valid
    TransactionInfo originalTransaction,
    
    // Credit Reason and Details
    @NotNull(message = "Credit details are required")
    @Valid
    CreditDetails creditDetails
) {
    
    public record RequesterInfo(
        @NotNull(message = "Requester ID is required")
        String requesterId,
        @NotNull(message = "Requester type is required")
        RequesterType requesterType,
        @NotNull(message = "Requester name is required")
        String name,
        String email,
        String department
    ) {}
    
    public record IssuerInfo(
        String companyName,
        String email,
        String phone,
        @Valid
        Address address,
        String accountNumber
    ) {}
    
    public record CustomerInfo(
        @NotNull(message = "Customer ID is required")
        String customerId,
        @NotNull(message = "Customer name is required")
        String customerName,
        String email,
        String phone,
        @Valid
        Address billingAddress,
        String accountNumber,
        @Valid
        BankDetails bankDetails  // Optional: Required only if customer banks with a different bank
    ) {}
    
    public record Address(
        String street,
        String city,
        String state,
        String zipCode,
        String country
    ) {}
    
    public record BankDetails(
        String bankName,
        String bankBranch,
        String sortCode,
        String swiftCode,
        String accountHolderName
    ) {}
    
    public record TransactionInfo(
        String transactionId,
        @NotNull(message = "Invoice number is required")
        String invoiceNumber,
        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,
        @NotNull(message = "Original amount is required")
        BigDecimal originalAmount,
        @NotNull(message = "Currency is required")
        String currency,
        @Valid
        List<LineItem> lineItems
    ) {}
    
    public record LineItem(
        String itemId,
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
    ) {}
    
    public record CreditDetails(
        @NotNull(message = "Credit reason is required")
        CreditReason reason,
        @NotNull(message = "Reason description is required")
        String reasonDescription,
        @NotNull(message = "Credit amount is required")
        BigDecimal creditAmount,
        List<String> affectedItems,
        String additionalNotes,
        boolean requiresApproval,
        String approverEmail
    ) {}
    
    public enum RequesterType {
        BUSINESS_CUSTOMER,
        BANK_COLLEAGUE,
        SYSTEM_AUTOMATED,
        CUSTOMER_SERVICE
    }
    
    public enum CreditReason {
        PRODUCT_RETURN,
        DEFECTIVE_GOODS,
        BILLING_ERROR,
        OVERCHARGE,
        PRICE_ADJUSTMENT,
        SERVICE_ISSUE,
        CANCELLATION,
        GOODWILL_GESTURE,
        OTHER
    }
}
