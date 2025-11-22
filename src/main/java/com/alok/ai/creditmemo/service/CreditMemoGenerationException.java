package com.alok.ai.creditmemo.service;

/**
 * Exception thrown when credit memo generation fails
 */
public class CreditMemoGenerationException extends RuntimeException {
    
    public CreditMemoGenerationException(String message) {
        super(message);
    }
    
    public CreditMemoGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
