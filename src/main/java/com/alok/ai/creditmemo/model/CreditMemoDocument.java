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
    String companyName,
    String companyAddress,
    
    CustomerDetails customer,
    OriginalInvoiceReference originalInvoice,
    CreditInformation creditInfo,
    
    List<CreditLineItem> creditLineItems,
    
    FinancialSummary financialSummary,
    
    String termsAndConditions,
    String authorizedBy,
    String notes
) {
    
    public record CustomerDetails(
        String customerId,
        String name,
        String address,
        String email,
        String phone
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
