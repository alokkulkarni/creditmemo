package com.alok.ai.creditmemo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request model for credit memo generation
 */
public record CreditMemoRequest(
    // Requester Information
    RequesterInfo requester,
    
    // Customer Information
    CustomerInfo customer,
    
    // Original Transaction Details
    TransactionInfo originalTransaction,
    
    // Credit Reason and Details
    CreditDetails creditDetails
) {
    
    public record RequesterInfo(
        String requesterId,
        RequesterType requesterType,
        String name,
        String email,
        String department
    ) {}
    
    public record CustomerInfo(
        String customerId,
        String customerName,
        String email,
        String phone,
        Address billingAddress,
        String accountNumber
    ) {}
    
    public record Address(
        String street,
        String city,
        String state,
        String zipCode,
        String country
    ) {}
    
    public record TransactionInfo(
        String transactionId,
        String invoiceNumber,
        LocalDate transactionDate,
        BigDecimal originalAmount,
        String currency,
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
        CreditReason reason,
        String reasonDescription,
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
