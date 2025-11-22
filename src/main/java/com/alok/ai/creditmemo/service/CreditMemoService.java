package com.alok.ai.creditmemo.service;

import com.alok.ai.creditmemo.model.CreditMemoDocument;
import com.alok.ai.creditmemo.model.CreditMemoRequest;
import com.alok.ai.creditmemo.model.CreditMemoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    
    public CreditMemoService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
        logger.info("CreditMemoService initialized with ChatClient");
    }
    
    /**
     * Generate a credit memo based on the provided request
     */
    public CreditMemoResponse generateCreditMemo(CreditMemoRequest request) {
        logger.info("Generating credit memo for customer: {}, requester type: {}", 
                    request.customer().customerId(), 
                    request.requester().requesterType());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Build the prompt for credit memo generation
            String prompt = buildCreditMemoPrompt(request);
            
            // Create output converter for structured response
            BeanOutputConverter<CreditMemoDocument> outputConverter = 
                new BeanOutputConverter<>(CreditMemoDocument.class);
            
            // Call Bedrock via Spring AI
            CreditMemoDocument document = chatClient.prompt()
                .user(prompt)
                .call()
                .entity(CreditMemoDocument.class);
            
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
    public String generateCreditMemoSummary(CreditMemoRequest request) {
        logger.info("Generating credit memo summary for customer: {}", 
                    request.customer().customerId());
        
        String summaryPrompt = buildSummaryPrompt(request);
        
        return chatClient.prompt()
            .user(summaryPrompt)
            .call()
            .content();
    }
    
    /**
     * Validate credit memo request using AI
     */
    public ValidationResult validateCreditMemoRequest(CreditMemoRequest request) {
        String validationPrompt = buildValidationPrompt(request);
        
        BeanOutputConverter<ValidationResult> outputConverter = 
            new BeanOutputConverter<>(ValidationResult.class);
        
        return chatClient.prompt()
            .user(validationPrompt)
            .call()
            .entity(ValidationResult.class);
    }
    
    private String buildCreditMemoPrompt(CreditMemoRequest request) {
        return String.format("""
            You are a financial document specialist tasked with generating a professional credit memo.
            
            COMPANY CONTEXT:
            This credit memo is being issued by our financial institution.
            
            REQUESTER INFORMATION:
            - Requester Type: %s
            - Name: %s
            - Department: %s
            
            CUSTOMER INFORMATION:
            - Customer ID: %s
            - Name: %s
            - Email: %s
            - Account Number: %s
            - Address: %s, %s, %s %s, %s
            
            ORIGINAL TRANSACTION:
            - Transaction ID: %s
            - Invoice Number: %s
            - Transaction Date: %s
            - Original Amount: %s %s
            - Number of Line Items: %d
            
            CREDIT REASON:
            - Reason: %s
            - Description: %s
            - Credit Amount: %s %s
            - Additional Notes: %s
            
            REQUIREMENTS:
            1. Generate a complete, professional credit memo document
            2. Include all relevant financial details
            3. Use formal business language
            4. Ensure accuracy of all amounts and calculations
            5. Include clear explanation of the credit reason
            6. Add appropriate terms and conditions
            7. Credit memo number should follow format: CM-[YEAR]-[SEQUENCE]
            8. Issue date should be today's date
            
            Generate the credit memo as a structured document that can be officially issued to the customer.
            Ensure all financial calculations are accurate and all required fields are populated.
            """,
            request.requester().requesterType(),
            request.requester().name(),
            request.requester().department(),
            request.customer().customerId(),
            request.customer().customerName(),
            request.customer().email(),
            request.customer().accountNumber(),
            request.customer().billingAddress().street(),
            request.customer().billingAddress().city(),
            request.customer().billingAddress().state(),
            request.customer().billingAddress().zipCode(),
            request.customer().billingAddress().country(),
            request.originalTransaction().transactionId(),
            request.originalTransaction().invoiceNumber(),
            request.originalTransaction().transactionDate(),
            request.originalTransaction().originalAmount(),
            request.originalTransaction().currency(),
            request.originalTransaction().lineItems().size(),
            request.creditDetails().reason(),
            request.creditDetails().reasonDescription(),
            request.creditDetails().creditAmount(),
            request.originalTransaction().currency(),
            request.creditDetails().additionalNotes()
        );
    }
    
    private String buildSummaryPrompt(CreditMemoRequest request) {
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
    
    private String buildValidationPrompt(CreditMemoRequest request) {
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
    
    private CreditMemoResponse buildResponse(CreditMemoRequest request, 
                                             CreditMemoDocument document, 
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
    
    private String formatDocumentAsString(CreditMemoDocument document) {
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
        document.creditLineItems().forEach(item -> {
            sb.append(String.format("- %s (Qty: %d) @ %s = %s - %s\n",
                item.itemDescription(),
                item.quantity(),
                item.unitPrice(),
                item.lineTotal(),
                item.reasonForCredit()));
        });
        
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
