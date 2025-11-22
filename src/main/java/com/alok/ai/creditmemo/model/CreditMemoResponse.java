package com.alok.ai.creditmemo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response model containing the generated credit memo
 */
public record CreditMemoResponse(
    String creditMemoId,
    String creditMemoNumber,
    LocalDateTime generatedAt,
    CreditMemoStatus status,
    
    // Reference to original request
    String originalInvoiceNumber,
    String customerId,
    String customerName,
    
    // Credit Details
    BigDecimal creditAmount,
    String currency,
    String reason,
    
    // Generated Content
    String creditMemoDocument,
    String summary,
    
    // Approval workflow
    boolean requiresApproval,
    String approvalStatus,
    
    // Metadata
    String generatedBy,
    ProcessingMetadata metadata
) {
    
    public record ProcessingMetadata(
        long processingTimeMs,
        String model,
        int tokensUsed,
        String requestId
    ) {}
    
    public enum CreditMemoStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        ISSUED,
        REJECTED,
        CANCELLED
    }
}
