package com.alok.ai.creditmemo.controller;

import com.alok.ai.creditmemo.model.CreditMemoRequest;
import com.alok.ai.creditmemo.model.CreditMemoResponse;
import com.alok.ai.creditmemo.service.CreditMemoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for credit memo generation
 * Handles requests from business customers, bank colleagues, and other systems
 */
@RestController
@RequestMapping("/api/v1/credit-memos")
public class CreditMemoController {
    
    private static final Logger logger = LoggerFactory.getLogger(CreditMemoController.class);
    
    private final CreditMemoService creditMemoService;
    
    public CreditMemoController(CreditMemoService creditMemoService) {
        this.creditMemoService = creditMemoService;
    }
    
    /**
     * Generate a new credit memo
     * POST /api/v1/credit-memos
     */
    @PostMapping
    public ResponseEntity<CreditMemoResponse> generateCreditMemo(
            @Valid @RequestBody CreditMemoRequest request) {
        
        logger.info("Received credit memo generation request from {} for customer {}", 
                    request.requester().requesterType(),
                    request.customer().customerId());
        
        try {
            CreditMemoResponse response = creditMemoService.generateCreditMemo(request);
            
            logger.info("Successfully generated credit memo {} for customer {}", 
                       response.creditMemoNumber(),
                       response.customerId());
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
                
        } catch (Exception e) {
            logger.error("Failed to generate credit memo for customer {}", 
                        request.customer().customerId(), e);
            throw e;
        }
    }
    
    /**
     * Generate a credit memo summary only (no full document)
     * POST /api/v1/credit-memos/summary
     */
    @PostMapping("/summary")
    public ResponseEntity<SummaryResponse> generateCreditMemoSummary(
            @Valid @RequestBody CreditMemoRequest request) {
        
        logger.info("Received credit memo summary request from {} for customer {}", 
                    request.requester().requesterType(),
                    request.customer().customerId());
        
        String summary = creditMemoService.generateCreditMemoSummary(request);
        
        return ResponseEntity.ok(new SummaryResponse(
            request.customer().customerId(),
            request.originalTransaction().invoiceNumber(),
            summary
        ));
    }
    
    /**
     * Validate a credit memo request before generation
     * POST /api/v1/credit-memos/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<CreditMemoService.ValidationResult> validateCreditMemoRequest(
            @Valid @RequestBody CreditMemoRequest request) {
        
        logger.info("Validating credit memo request for customer {}", 
                    request.customer().customerId());
        
        CreditMemoService.ValidationResult result = 
            creditMemoService.validateCreditMemoRequest(request);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Health check endpoint
     * GET /api/v1/credit-memos/health
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP", "Credit Memo Service is operational"));
    }
    
    // Response DTOs
    public record SummaryResponse(
        String customerId,
        String invoiceNumber,
        String summary
    ) {}
    
    public record HealthResponse(
        String status,
        String message
    ) {}
}
