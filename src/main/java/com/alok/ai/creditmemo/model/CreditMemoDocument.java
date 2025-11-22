package com.alok.ai.creditmemo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Structured credit memo document format for AI-generated output
 */
public record CreditMemoDocument(
    String creditMemoNumber,
    LocalDate issueDate,
    
    // Issuer: Either the bank or the business customer issuing the credit memo
    IssuerDetails issuer,
    
    // Recipient: The customer receiving the credit memo
    RecipientDetails recipient,
    
    OriginalInvoiceReference originalInvoice,
    CreditInformation creditInfo,
    
    List<CreditLineItem> creditLineItems,
    
    FinancialSummary financialSummary,
    
    String termsAndConditions,
    String authorizedBy,
    String notes
) {
    
    public record IssuerDetails(
        String name,
        String address,
        String email,
        String phone,
        String accountNumber
    ) {}
    
    public record RecipientDetails(
        String customerId,
        String name,
        String address,
        String email,
        String phone,
        String accountNumber,
        BankDetails bankDetails  // Optional: Bank details if customer banks elsewhere
    ) {}
    
    public record BankDetails(
        String bankName,
        String bankBranch,
        String sortCode,
        String swiftCode,
        String accountHolderName
    ) {}
    
    public record OriginalInvoiceReference(
        String invoiceNumber,
        LocalDate invoiceDate,
        BigDecimal originalAmount
    ) {}
    
    public record CreditInformation(
        String reason,
        String detailedExplanation,
        String creditType // FULL or PARTIAL
    ) {}
    
    public record CreditLineItem(
        String itemDescription,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String reasonForCredit
    ) {}
    
    public record FinancialSummary(
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalCreditAmount,
        String currency
    ) {}
}
