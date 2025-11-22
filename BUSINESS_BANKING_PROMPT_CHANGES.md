# Business Banking Prompt Implementation

## Overview
Updated the credit memo generation service to follow **industry best practices** for UK business banking credit memos with comprehensive structured output, detailed line items, proper financial calculations, and professional narratives.

## Latest Changes (22 Nov 2025)

### 🔧 Bug Fixes
- **Fixed NullPointerException**: Added null safety check for `creditLineItems` before iteration
- **Enhanced Error Handling**: Graceful handling when line items are missing

### 🎯 Industry Best Practices Implementation
1. **Comprehensive Credit Memo Structure**
   - Full header with company details and credit memo numbering
   - Detailed customer information including all contact details
   - Complete original invoice reference with line-by-line breakdown
   - Professional credit explanation with business context
   - Line item breakdown showing quantities, prices, and individual reasons
   - Financial summary with subtotal, tax (20% UK VAT), and total credit
   - Standard UK banking terms and conditions
   - Clear authorization trail

2. **Professional Narrative**
   - 3-4 sentence detailed explanation covering:
     - What is being credited
     - Why the credit is being issued
     - Impact on customer account
     - Any follow-up actions if applicable
   - Formal business tone suitable for UK banking standards

3. **Financial Accuracy**
   - Automatic calculation of subtotal and tax (20% UK VAT)
   - Support for both FULL and PARTIAL credit types
   - Proper decimal formatting without commas
   - Currency consistency throughout document

4. **Line Item Detail**
   - Each credited item includes:
     - Item description
     - Quantity
     - Unit price
     - Line total
     - Specific reason for credit
   - Built from original transaction line items

## Changes Made

### 1. System Prompt Update (`SpringAiConfig.java`)
**Purpose**: Set the AI's role with industry best practices and strict JSON compliance

**Enhanced Rules**:
- ✅ Never invent financial values, dates, or customer details
- ✅ Valid JSON matching exact schema required
- ✅ No extra commentary outside JSON structure
- ✅ Proper numeric formatting (decimals, no commas)
- ✅ Formal UK business banking tone
- ✅ No advice or speculative statements

**Industry Best Practices**:
- ✅ Detailed narrative explaining reason in business context
- ✅ Line-by-line breakdown of credited items
- ✅ Clear financial summary with subtotal, tax, and total
- ✅ Professional terms and conditions
- ✅ Proper authorization trail
- ✅ Full original transaction reference

### 2. Credit Memo Prompt Update (`CreditMemoService.java`)
**Purpose**: Generate comprehensive structured credit memos following UK banking standards

**Key Features**:
- **Context Detection**: Automatically determines credit memo purpose:
  - Business customer issuing to their own customer
  - Bank colleague correcting incorrect fee charges
  - System-automated processing (returns, adjustments)
  - Customer service on behalf of customer

- **Comprehensive Data Collection**:
  - Full customer details with billing address
  - Complete original transaction information
  - Detailed line items with descriptions, quantities, and prices
  - Authorization information with requester details

- **Automatic Financial Calculations**:
  - Credit type determination (FULL vs PARTIAL)
  - Subtotal calculation (amount excluding VAT)
  - Tax calculation (20% UK VAT)
  - Total credit amount with tax included
  
- **Unique Credit Memo Numbering**:
  - Format: `CM-YYYY-XXXXXXXX`
  - Example: `CM-2025-A3F7D9B1`
  - Year-based with UUID suffix for uniqueness

- **Industry-Standard JSON Structure**:
  ```json
  {
    "creditMemoNumber": "CM-2025-A3F7D9B1",
    "issueDate": "2025-11-22",
    "companyName": "UK Business Bank PLC",
    "companyAddress": "1 Bank Street, London, EC2R 8AH, United Kingdom",
    "customer": {
      "customerId": "CUST67890",
      "name": "TechStart Inc",
      "address": "456 Innovation Drive, San Francisco, CA 94105, USA",
      "email": "finance@techstart.com",
      "phone": "+1-555-0200"
    },
    "originalInvoice": {
      "invoiceNumber": "INV-2024-045",
      "invoiceDate": "2024-11-01",
      "originalAmount": 12000.00
    },
    "creditInfo": {
      "reason": "PRODUCT_RETURN",
      "detailedExplanation": "This credit memo is issued for the return of 3 unused software licenses within the 30-day return window. The licenses (ITEM100 - Software License Annual Subscription) were never activated by the customer. The credit amount of £3,600.00 represents 30% of the original transaction value and will be applied to the customer's account within 5-7 business days, appearing on their next statement.",
      "creditType": "PARTIAL"
    },
    "creditLineItems": [
      {
        "itemDescription": "Software License - Annual Subscription (Returned)",
        "quantity": 3,
        "unitPrice": 1000.00,
        "lineTotal": 3000.00,
        "reasonForCredit": "Product returned unused within 30-day return window"
      }
    ],
    "financialSummary": {
      "subtotal": 3000.00,
      "taxAmount": 600.00,
      "totalCreditAmount": 3600.00,
      "currency": "USD"
    },
    "termsAndConditions": "This credit memo will be applied to your account within 5-7 business days. The credited amount will be reflected in your next statement. For queries, please contact our customer service team at customerservice@ukbusinessbank.com or call 0800-123-4567. Credit memo issued in accordance with UK business banking regulations and FCA guidelines.",
    "authorizedBy": "Automated Return System",
    "notes": "Return approved. 3 of 10 licenses returned. Customer retaining 7 licenses."
  }
  ```

- **Null Safety**: Handles missing or null line items gracefully

### 3. Technical Improvements
- Added `BigDecimal` import for proper financial amount comparison
- Implemented automatic tax calculation (20% UK VAT)
- Added credit type determination (FULL vs PARTIAL)
- Unique credit memo number generation with UUID
- Comprehensive line item detail building from original transaction
- **Null safety for creditLineItems array** (prevents NullPointerException)
- Maintained @NonNull annotations throughout
- Proper decimal formatting and rounding for financial calculations

## Business Context

### Use Cases Supported
1. **Business Banking Customers**: Issuing credit memos to their own customers
2. **Bank Colleagues**: Correcting incorrect fee charges posted by the bank
3. **System Automated**: Auto-generated credit memos for bulk processing
4. **Customer Service**: Staff issuing credit memos on behalf of customers

### UK Business Banking Compliance
- Formal professional tone suitable for UK financial institutions
- No invented or speculative data (regulatory compliance)
- Clear audit trail with requester and context information
- Structured JSON output for downstream system integration

## Testing Instructions

### 1. Start Application
```bash
cd /Users/alokkulkarni/Documents/Development/creditmemo
./mvnw spring-boot:run
```

### 2. Run Complete Test Suite
```bash
./test-api.sh
```

### 3. Expected Outputs

#### Test 2: Generate Credit Memo - Billing Error
- Complete CreditMemoDocument JSON structure
- Detailed narrative explaining billing error context
- Line items with individual credit reasons
- Financial summary with subtotal, tax, and total
- Professional UK banking terms and conditions

#### Test 3: Generate Credit Memo Summary
- 2-3 sentence concise summary
- Customer and invoice reference
- Credit amount and reason
- Requester context

#### Test 4: Validate Credit Memo Request
- Risk assessment (LOW/MEDIUM/HIGH)
- Validation issues identified
- Business recommendations
- Approval requirements based on amount and risk

#### Test 5: Generate Credit Memo - Product Return
- **Should now succeed** (fixed NullPointerException)
- Complete structured credit memo with:
  - All customer and company details
  - Original invoice reference
  - 3-4 sentence professional explanation
  - Line item breakdown (3 returned licenses)
  - Financial calculations (subtotal: £3000, tax: £600, total: £3600)
  - Standard UK banking T&Cs
  - Authorization by Automated Return System

### 4. Verify JSON Output Quality
Check that responses contain:
- ✅ Valid JSON only (no markdown, no code blocks)
- ✅ No extra explanatory text
- ✅ No invented data
- ✅ Professional UK business banking narrative
- ✅ All numeric values as decimals (no commas)
- ✅ Complete line item breakdown
- ✅ Accurate financial calculations
- ✅ Proper date formatting (YYYY-MM-DD)

## Next Steps

### Immediate Priorities
1. ✅ Fixed NullPointerException in creditLineItems
2. ✅ Implemented industry best practices for credit memo structure
3. ✅ Added comprehensive financial calculations
4. ⏳ Test application with updated prompts and verify all endpoints
5. ⏳ Validate professional narrative quality in generated memos
6. ⏳ Confirm line item breakdown appears correctly

### Future Enhancements

#### 1. PDF Generation
Implement PDF rendering for downloadable credit memos:
```java
// Add dependency
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
</dependency>
```

**Features**:
- Use JSON response to populate professional PDF template
- UK Business Bank letterhead and branding
- Proper formatting for financial figures
- Digital signature capability
- Add endpoint: `POST /api/v1/credit-memos/generate-pdf`

**Implementation Steps**:
1. Create PDF template service
2. Map CreditMemoDocument to PDF layout
3. Add company logo and branding
4. Include barcode/QR code for verification
5. Return PDF as downloadable file or base64

#### 2. Response Parsing Enhancement
Add stronger type safety for AI responses:
```java
// Create wrapper for AI JSON validation
public class CreditMemoJsonValidator {
    public static CreditMemoDocument validate(String json) {
        // Parse and validate against schema
        // Ensure all required fields present
        // Validate financial calculations
        // Return strongly-typed object
    }
}
```

#### 3. Inference Profile Support
Enable Claude 4.5 usage with inference profiles:
- Configure AWS Bedrock inference profile ARN in application.yaml
- Support for cross-region inference profiles
- Fallback mechanism if profile unavailable

#### 4. Multi-Region Support
Test and enable eu-west-2 for UK data residency:
- Request Claude 3.5 Sonnet access in eu-west-2
- Configure region failover (eu-west-2 → us-east-1)
- Monitor latency differences
- Ensure FCA compliance with UK data residency

#### 5. Enhanced Validation
Improve credit memo validation logic:
- Check credit amount vs original amount ratio
- Validate affected items exist in original transaction
- Verify requester authorization level for high-value credits
- FCA compliance checks for UK banking regulations
- Automatic escalation for credits exceeding thresholds

#### 6. Audit Trail
Implement comprehensive audit logging:
- Store all generated credit memos
- Track requester and authorization chain
- Record AI model used and response time
- Maintain change history
- Generate audit reports for compliance

## Configuration Reference

### Current AWS Bedrock Configuration
```yaml
spring:
  ai:
    bedrock:
      aws:
        region: us-east-1  # Changed from eu-west-2 due to connectivity
      anthropic:
        chat:
          model: anthropic.claude-3-5-sonnet-20240620-v1:0
          options:
            temperature: 0.3
            max-tokens: 4096
            timeout: 120s  # Increased from 60s
```

### Model Information
- **Model**: Claude 3.5 Sonnet
- **Provider**: AWS Bedrock Anthropic
- **Region**: us-east-1 (most reliable for Bedrock)
- **Temperature**: 0.3 (low for consistent, factual outputs)
- **Max Tokens**: 4096 (sufficient for credit memo JSON)
- **Timeout**: 120s (allows for complex generation)

## Support

### Common Issues
1. **Non-JSON Output**: Check system prompt is loaded correctly
2. **Invented Data**: Verify temperature is 0.3 or lower
3. **Timeout Errors**: Increase timeout in application.yaml
4. **Region Issues**: Ensure us-east-1 is accessible from your network

### Debugging
Enable detailed logging:
```yaml
logging:
  level:
    com.alok.ai.creditmemo: DEBUG
    org.springframework.ai: DEBUG
```

Review AI diagnostic information when latency is high or unexpected status codes occur.
