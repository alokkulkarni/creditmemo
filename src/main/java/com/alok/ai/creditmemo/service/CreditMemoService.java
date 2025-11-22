package com.alok.ai.creditmemo.service;

import com.alok.ai.creditmemo.model.CreditMemoDocument;
import com.alok.ai.creditmemo.model.CreditMemoRequest;
import com.alok.ai.creditmemo.model.CreditMemoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for generating credit memos using AWS Bedrock via Spring AI
 */
@Service
public class CreditMemoService {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditMemoService.class);
    
    private final ChatClient chatClient;
    
    @Value("${spring.ai.bedrock.converse.chat.model:anthropic.claude-3-5-sonnet-20240620-v1:0}")
    private String modelName;
    
    public CreditMemoService(@NonNull ChatModel chatModel) {
        Objects.requireNonNull(chatModel, "ChatModel must not be null");
        this.chatClient = ChatClient.builder(chatModel).build();
        logger.info("CreditMemoService initialized with ChatClient");
    }
    
    /**
     * Generate a credit memo based on the provided request
     */
    public CreditMemoResponse generateCreditMemo(@NonNull CreditMemoRequest request) {
        Objects.requireNonNull(request, "CreditMemoRequest must not be null");
        
        logger.info("Generating credit memo for customer: {}, requester type: {}", 
                    request.customer().customerId(), 
                    request.requester().requesterType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build the prompt for credit memo generation
            String prompt = buildCreditMemoPrompt(request);
            Objects.requireNonNull(prompt, "Generated prompt must not be null");
            
            // Call Bedrock via Spring AI
            CreditMemoDocument document = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(CreditMemoDocument.class);
            
            Objects.requireNonNull(document, "AI failed to generate credit memo document");
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Build response
            return buildResponse(request, document, processingTime);
            
        } catch (Exception e) {
            logger.error("Error generating credit memo", e);
            throw new CreditMemoGenerationException("Failed to generate credit memo: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a summary of the credit memo without full document
     */
    public String generateCreditMemoSummary(@NonNull CreditMemoRequest request) {
        Objects.requireNonNull(request, "CreditMemoRequest must not be null");
        
        logger.info("Generating credit memo summary for customer: {}", 
                    request.customer().customerId());
        
        String summaryPrompt = buildSummaryPrompt(request);
        Objects.requireNonNull(summaryPrompt, "Summary prompt must not be null");
        
        return chatClient.prompt()
            .user(summaryPrompt)
            .call()
            .content();
    }
    
    /**
     * Validate credit memo request using AI
     */
    public ValidationResult validateCreditMemoRequest(@NonNull CreditMemoRequest request) {
        Objects.requireNonNull(request, "CreditMemoRequest must not be null");
        
        String validationPrompt = buildValidationPrompt(request);
        Objects.requireNonNull(validationPrompt, "Validation prompt must not be null");
        
        return chatClient.prompt()
            .user(validationPrompt)
            .call()
            .entity(ValidationResult.class);
    }
    
    @NonNull
    @SuppressWarnings("null")
    private String buildCreditMemoPrompt(@NonNull CreditMemoRequest request) {
        // Determine context based on requester type
        String contextNote = switch (request.requester().requesterType()) {
            case BUSINESS_CUSTOMER -> "This credit memo is issued by a business banking customer for their own customer.";
            case BANK_COLLEAGUE -> "This credit memo corrects an incorrect fee charge posted by the bank.";
            case SYSTEM_AUTOMATED -> "This credit memo is system-generated for automated processing.";
            case CUSTOMER_SERVICE -> "This credit memo is issued by customer service on behalf of the customer.";
        };
        
        // Build detailed line items information
        StringBuilder lineItemsDetail = new StringBuilder();
        if (request.originalTransaction().lineItems() != null && !request.originalTransaction().lineItems().isEmpty()) {
            request.originalTransaction().lineItems().forEach(item -> {
                lineItemsDetail.append(String.format("\n  - Item: %s | Description: %s | Qty: %d | Unit Price: %.2f %s | Total: %.2f %s",
                    item.itemId(),
                    item.description(),
                    item.quantity(),
                    item.unitPrice(),
                    request.originalTransaction().currency(),
                    item.totalPrice(),
                    request.originalTransaction().currency()));
            });
        }
        
        // Calculate tax (assuming 20% UK VAT for business transactions)
        BigDecimal creditAmount = request.creditDetails().creditAmount();
        BigDecimal taxRate = new BigDecimal("0.20");
        BigDecimal subtotal = creditAmount.divide(BigDecimal.ONE.add(taxRate), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal taxAmount = creditAmount.subtract(subtotal);
        
        // Determine credit type
        String creditType = creditAmount.compareTo(request.originalTransaction().originalAmount()) >= 0 ? "FULL" : "PARTIAL";
        
        // Generate unique credit memo number
        String creditMemoNumber = String.format("CM-%s-%s", 
            LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy")),
            UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        return String.format("""
            Generate a professional UK business banking credit memo using the following data:
            
            === CONTEXT ===
            %s
            
            === CREDIT MEMO DETAILS ===
            Credit Memo Number: %s
            Issue Date: %s
            Company: UK Business Bank PLC
            Company Address: 1 Bank Street, London, EC2R 8AH, United Kingdom
            
            === CUSTOMER INFORMATION ===
            Customer ID: %s
            Customer Name: %s
            Customer Email: %s
            Customer Phone: %s
            Billing Address: %s, %s, %s %s, %s
            Account Number: %s
            
            === ORIGINAL TRANSACTION ===
            Transaction ID: %s
            Invoice Number: %s
            Invoice Date: %s
            Original Amount: %.2f %s
            Line Items: %s
            
            === CREDIT DETAILS ===
            Credit Type: %s
            Credit Reason: %s
            Reason Description: %s
            Credit Amount (incl. tax): %.2f %s
            Subtotal (excl. tax): %.2f %s
            Tax Amount (20%% VAT): %.2f %s
            Affected Items: %s
            Additional Notes: %s
            
            === AUTHORIZATION ===
            Requested By: %s
            Requester Type: %s
            Requester Email: %s
            Department: %s
            
            
            YOU MUST OUTPUT ONLY THE FOLLOWING JSON STRUCTURE (NO OTHER TEXT):
            {
              "creditMemoNumber": "%s",
              "issueDate": "%s",
              "companyName": "UK Business Bank PLC",
              "companyAddress": "1 Bank Street, London, EC2R 8AH, United Kingdom",
              "customer": {
                "customerId": "%s",
                "name": "%s",
                "address": "%s, %s, %s %s, %s",
                "email": "%s",
                "phone": "%s"
              },
              "originalInvoice": {
                "invoiceNumber": "%s",
                "invoiceDate": "%s",
                "originalAmount": %.2f
              },
              "creditInfo": {
                "reason": "%s",
                "detailedExplanation": "[Write a professional 3-4 sentence explanation suitable for UK business banking. Include: (1) What is being credited, (2) Why the credit is being issued, (3) Impact on customer account, (4) Any follow-up actions if applicable. Use formal business tone.]",
                "creditType": "%s"
              },
              "creditLineItems": [
                [FOR EACH affected item, create an entry with format:]
                {
                  "itemDescription": "[Item description from line items]",
                  "quantity": [quantity as integer],
                  "unitPrice": [unit price as decimal],
                  "lineTotal": [line total as decimal],
                  "reasonForCredit": "[Specific reason for this item's credit]"
                }
              ],
              "financialSummary": {
                "subtotal": %.2f,
                "taxAmount": %.2f,
                "totalCreditAmount": %.2f,
                "currency": "%s"
              },
              "termsAndConditions": "This credit memo will be applied to your account within 5-7 business days. The credited amount will be reflected in your next statement. For queries, please contact our customer service team at customerservice@ukbusinessbank.com or call 0800-123-4567. Credit memo issued in accordance with UK business banking regulations and FCA guidelines.",
              "authorizedBy": "%s",
              "notes": "%s"
            }
            
            CRITICAL REQUIREMENTS:
            1. Output ONLY valid JSON - no markdown, no code blocks, no explanations
            2. Use provided values exactly as shown
            3. creditLineItems array must contain at least one item based on the affected items
            4. detailedExplanation must be professional, factual, and specific to this transaction
            5. All numeric values must be decimals without commas
            6. Dates in YYYY-MM-DD format
            7. Do not invent any financial figures - calculate from provided data
            """,
            // Context
            contextNote,
            
            // Header information
            creditMemoNumber,
            LocalDateTime.now().toLocalDate(),
            
            // Customer information
            request.customer().customerId(),
            request.customer().customerName(),
            request.customer().email(),
            request.customer().phone() != null ? request.customer().phone() : "N/A",
            request.customer().billingAddress().street(),
            request.customer().billingAddress().city(),
            request.customer().billingAddress().state(),
            request.customer().billingAddress().zipCode(),
            request.customer().billingAddress().country(),
            request.customer().accountNumber(),
            
            // Original transaction
            request.originalTransaction().transactionId(),
            request.originalTransaction().invoiceNumber(),
            request.originalTransaction().transactionDate(),
            request.originalTransaction().originalAmount(),
            request.originalTransaction().currency(),
            lineItemsDetail.toString(),
            
            // Credit details
            creditType,
            request.creditDetails().reason(),
            request.creditDetails().reasonDescription(),
            creditAmount,
            request.originalTransaction().currency(),
            subtotal,
            request.originalTransaction().currency(),
            taxAmount,
            request.originalTransaction().currency(),
            request.creditDetails().affectedItems() != null ? String.join(", ", request.creditDetails().affectedItems()) : "All items",
            request.creditDetails().additionalNotes(),
            
            // Authorization
            request.requester().name(),
            request.requester().requesterType(),
            request.requester().email(),
            request.requester().department() != null ? request.requester().department() : "N/A",
            
            // JSON template values
            creditMemoNumber,
            LocalDateTime.now().toLocalDate(),
            request.customer().customerId(),
            request.customer().customerName(),
            request.customer().billingAddress().street(),
            request.customer().billingAddress().city(),
            request.customer().billingAddress().state(),
            request.customer().billingAddress().zipCode(),
            request.customer().billingAddress().country(),
            request.customer().email(),
            request.customer().phone() != null ? request.customer().phone() : "N/A",
            request.originalTransaction().invoiceNumber(),
            request.originalTransaction().transactionDate(),
            request.originalTransaction().originalAmount(),
            request.creditDetails().reason(),
            creditType,
            subtotal,
            taxAmount,
            creditAmount,
            request.originalTransaction().currency(),
            request.requester().name(),
            request.creditDetails().additionalNotes()
        );
    }
    
    @NonNull
    @SuppressWarnings("null")
    private String buildSummaryPrompt(@NonNull CreditMemoRequest request) {
        return String.format("""
            Provide a brief 2-3 sentence summary of this credit memo request:
            
            - Customer: %s (ID: %s)
            - Original Invoice: %s
            - Credit Reason: %s
            - Credit Amount: %s %s
            - Requester: %s (%s)
            
            Summarize the key details in a professional manner suitable for management review.
            """,
            request.customer().customerName(),
            request.customer().customerId(),
            request.originalTransaction().invoiceNumber(),
            request.creditDetails().reason(),
            request.creditDetails().creditAmount(),
            request.originalTransaction().currency(),
            request.requester().name(),
            request.requester().requesterType()
        );
    }
    
    @NonNull
    @SuppressWarnings("null")
    private String buildValidationPrompt(@NonNull CreditMemoRequest request) {
        return String.format("""
            Validate this credit memo request and identify any issues or concerns:
            
            - Credit Amount: %s (Original Transaction: %s)
            - Reason: %s - %s
            - Requester Type: %s
            - Requires Approval: %s
            
            Analyze:
            1. Is the credit amount reasonable compared to the original transaction?
            2. Is the reason clearly explained and justified?
            3. Are there any red flags or concerns?
            4. Should this require additional approval?
            
            Return a validation result with isValid (boolean), issues (list of strings), 
            recommendations (list of strings), and riskLevel (LOW, MEDIUM, HIGH).
            """,
            request.creditDetails().creditAmount(),
            request.originalTransaction().originalAmount(),
            request.creditDetails().reason(),
            request.creditDetails().reasonDescription(),
            request.requester().requesterType(),
            request.creditDetails().requiresApproval()
        );
    }
    
    @NonNull
    private CreditMemoResponse buildResponse(@NonNull CreditMemoRequest request, 
                                             @NonNull CreditMemoDocument document, 
                                             long processingTime) {
        String creditMemoId = UUID.randomUUID().toString();
        
        CreditMemoResponse.CreditMemoStatus status = 
            request.creditDetails().requiresApproval() 
                ? CreditMemoResponse.CreditMemoStatus.PENDING_APPROVAL 
                : CreditMemoResponse.CreditMemoStatus.DRAFT;
        
        String summary = generateCreditMemoSummary(request);
        
        return new CreditMemoResponse(
            creditMemoId,
            document.creditMemoNumber(),
            LocalDateTime.now(),
            status,
            request.originalTransaction().invoiceNumber(),
            request.customer().customerId(),
            request.customer().customerName(),
            request.creditDetails().creditAmount(),
            request.originalTransaction().currency(),
            request.creditDetails().reason().toString(),
            formatDocumentAsString(document),
            summary,
            request.creditDetails().requiresApproval(),
            status.name(),
            request.requester().name(),
            new CreditMemoResponse.ProcessingMetadata(
                processingTime,
                modelName,
                0, // Token count would need to be extracted from response metadata
                creditMemoId
            )
        );
    }
    
    @NonNull
    @SuppressWarnings("null")
    private String formatDocumentAsString(@NonNull CreditMemoDocument document) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CREDIT MEMO ===\n\n");
        sb.append("Credit Memo Number: ").append(document.creditMemoNumber()).append("\n");
        sb.append("Issue Date: ").append(document.issueDate()).append("\n");
        sb.append("Company: ").append(document.companyName()).append("\n");
        sb.append("Address: ").append(document.companyAddress()).append("\n\n");
        
        sb.append("CUSTOMER INFORMATION:\n");
        sb.append("Name: ").append(document.customer().name()).append("\n");
        sb.append("Customer ID: ").append(document.customer().customerId()).append("\n");
        sb.append("Address: ").append(document.customer().address()).append("\n\n");
        
        sb.append("ORIGINAL INVOICE REFERENCE:\n");
        sb.append("Invoice Number: ").append(document.originalInvoice().invoiceNumber()).append("\n");
        sb.append("Invoice Date: ").append(document.originalInvoice().invoiceDate()).append("\n");
        sb.append("Original Amount: ").append(document.originalInvoice().originalAmount()).append("\n\n");
        
        sb.append("CREDIT REASON:\n");
        sb.append(document.creditInfo().reason()).append("\n");
        sb.append(document.creditInfo().detailedExplanation()).append("\n\n");
        
        sb.append("CREDIT LINE ITEMS:\n");
        if (document.creditLineItems() != null && !document.creditLineItems().isEmpty()) {
            document.creditLineItems().forEach(item -> {
                sb.append(String.format("- %s (Qty: %d) @ %s = %s - %s\n",
                    item.itemDescription(),
                    item.quantity(),
                    item.unitPrice(),
                    item.lineTotal(),
                    item.reasonForCredit()));
            });
        } else {
            sb.append("- No line item breakdown available\n");
        }
        
        sb.append("\nFINANCIAL SUMMARY:\n");
        sb.append("Subtotal: ").append(document.financialSummary().subtotal()).append("\n");
        sb.append("Tax: ").append(document.financialSummary().taxAmount()).append("\n");
        sb.append("TOTAL CREDIT: ").append(document.financialSummary().totalCreditAmount())
          .append(" ").append(document.financialSummary().currency()).append("\n\n");
        
        sb.append("Terms & Conditions: ").append(document.termsAndConditions()).append("\n");
        sb.append("Authorized By: ").append(document.authorizedBy()).append("\n");
        
        if (document.notes() != null && !document.notes().isEmpty()) {
            sb.append("Notes: ").append(document.notes()).append("\n");
        }
        
        return sb.toString();
    }
    
    public record ValidationResult(
        boolean isValid,
        java.util.List<String> issues,
        java.util.List<String> recommendations,
        String riskLevel
    ) {}
}
