# Credit Memo Generation System

## Overview

This Spring Boot application provides AI-powered credit memo generation using **Spring AI** with **AWS Bedrock Converse API**. The system accepts REST API requests from business customers, bank colleagues, or automated systems to generate professional credit memos.

## What is a Credit Memo?

A **credit memo** (credit memorandum) is a financial document issued by a seller to a buyer that indicates a credit to the buyer's account. Common scenarios include:

- **Product Returns**: Full or partial return of purchased goods
- **Defective Goods**: Compensation for damaged or defective products
- **Billing Errors**: Corrections to invoicing mistakes or overcharges
- **Price Adjustments**: Post-sale discounts or price corrections
- **Service Issues**: Credits for poor service or cancellations
- **Goodwill Gestures**: Customer satisfaction credits

## Technology Stack

- **Spring Boot 3.5.7** - Application framework
- **Spring AI 1.1.0** - AI integration layer
- **AWS Bedrock Converse API** - LLM provider
- **Claude 3.5 Sonnet** - Default AI model
- **Java 21** - Programming language
- **Maven** - Build tool

## Key Features

1. **AI-Powered Generation**: Uses AWS Bedrock (Claude 3.5 Sonnet) to generate professional credit memos
2. **Multiple Requester Types**: Supports requests from:
   - Business customers
   - Bank colleagues
   - Automated systems
   - Customer service representatives
3. **Structured Output**: Generates consistent, well-formatted credit memo documents
4. **Validation Support**: AI-assisted validation of credit memo requests
5. **Summary Generation**: Quick summaries for management review
6. **Approval Workflow**: Built-in support for approval requirements

## Architecture

```
┌─────────────────┐
│   REST Client   │ (Customer/Colleague/System)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  CreditMemo     │
│  Controller     │ (REST API Layer)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  CreditMemo     │
│  Service        │ (Business Logic)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Spring AI      │
│  ChatClient     │ (AI Integration)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  AWS Bedrock    │
│  Converse API   │ (Claude 3.5 Sonnet)
└─────────────────┘
```

## Configuration

### AWS Credentials

Set up AWS credentials using one of these methods:

**Option 1: Environment Variables**
```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

**Option 2: AWS CLI Configuration**
```bash
aws configure
```

**Option 3: IAM Roles** (for EC2/ECS/Lambda)
- Attach appropriate IAM role with Bedrock permissions

### Application Configuration

Edit `src/main/resources/application.yaml`:

```yaml
spring:
  ai:
    bedrock:
      converse:
        chat:
          model: anthropic.claude-3-5-sonnet-20240620-v1:0
          options:
            temperature: 0.7
            max-tokens: 4096
      aws:
        region: us-east-1
        timeout: 60s
```

## API Endpoints

### 1. Generate Credit Memo

**POST** `/api/v1/credit-memos`

Generates a complete credit memo document.

**Request Body:**
```json
{
  "requester": {
    "requesterId": "REQ001",
    "requesterType": "BANK_COLLEAGUE",
    "name": "John Smith",
    "email": "john.smith@bank.com",
    "department": "Customer Service"
  },
  "customer": {
    "customerId": "CUST12345",
    "customerName": "Acme Corporation",
    "email": "billing@acme.com",
    "phone": "+1-555-0100",
    "billingAddress": {
      "street": "123 Business Blvd",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "accountNumber": "ACC789456"
  },
  "originalTransaction": {
    "transactionId": "TXN001",
    "invoiceNumber": "INV-2024-001",
    "transactionDate": "2024-01-15",
    "originalAmount": 5000.00,
    "currency": "USD",
    "lineItems": [
      {
        "itemId": "ITEM001",
        "description": "Professional Services",
        "quantity": 1,
        "unitPrice": 5000.00,
        "totalPrice": 5000.00
      }
    ]
  },
  "creditDetails": {
    "reason": "BILLING_ERROR",
    "reasonDescription": "Incorrect pricing applied due to system error",
    "creditAmount": 500.00,
    "affectedItems": ["ITEM001"],
    "additionalNotes": "Customer has been notified of the correction",
    "requiresApproval": true,
    "approverEmail": "manager@bank.com"
  }
}
```

**Response (201 Created):**
```json
{
  "creditMemoId": "uuid-here",
  "creditMemoNumber": "CM-2024-001",
  "generatedAt": "2024-11-22T10:30:00",
  "status": "PENDING_APPROVAL",
  "originalInvoiceNumber": "INV-2024-001",
  "customerId": "CUST12345",
  "customerName": "Acme Corporation",
  "creditAmount": 500.00,
  "currency": "USD",
  "reason": "BILLING_ERROR",
  "creditMemoDocument": "=== CREDIT MEMO ===\n...",
  "summary": "Credit memo issued to Acme Corporation...",
  "requiresApproval": true,
  "approvalStatus": "PENDING_APPROVAL",
  "generatedBy": "John Smith",
  "metadata": {
    "processingTimeMs": 2500,
    "model": "anthropic.claude-3-5-sonnet-20240620-v1:0",
    "tokensUsed": 0,
    "requestId": "uuid-here"
  }
}
```

### 2. Generate Summary

**POST** `/api/v1/credit-memos/summary`

Generates a brief summary without the full document.

**Response:**
```json
{
  "customerId": "CUST12345",
  "invoiceNumber": "INV-2024-001",
  "summary": "Credit memo for $500.00 issued to Acme Corporation..."
}
```

### 3. Validate Request

**POST** `/api/v1/credit-memos/validate`

Validates a credit memo request before generation.

**Response:**
```json
{
  "isValid": true,
  "issues": [],
  "recommendations": [
    "Consider requiring manager approval for amounts over $1000"
  ],
  "riskLevel": "LOW"
}
```

### 4. Health Check

**GET** `/api/v1/credit-memos/health`

**Response:**
```json
{
  "status": "UP",
  "message": "Credit Memo Service is operational"
}
```

## Running the Application

### Prerequisites

1. Java 21 or higher
2. Maven 3.6+
3. AWS Account with Bedrock access
4. AWS credentials configured

### Build and Run

```bash
# Build the application
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Or run the JAR
java -jar target/creditmemo-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Testing the API

Use the sample requests in the `/samples` directory:

```bash
# Generate a credit memo
curl -X POST http://localhost:8080/api/v1/credit-memos \
  -H "Content-Type: application/json" \
  -d @samples/sample-request.json

# Get a summary
curl -X POST http://localhost:8080/api/v1/credit-memos/summary \
  -H "Content-Type: application/json" \
  -d @samples/sample-request.json

# Validate request
curl -X POST http://localhost:8080/api/v1/credit-memos/validate \
  -H "Content-Type: application/json" \
  -d @samples/sample-request.json
```

## Project Structure

```
src/main/java/com/alok/ai/creditmemo/
├── CreditmemoApplication.java
├── config/
│   ├── SpringAiConfig.java       # Spring AI configuration
│   └── WebConfig.java             # Web/CORS configuration
├── controller/
│   └── CreditMemoController.java  # REST endpoints
├── model/
│   ├── CreditMemoRequest.java     # Request DTOs
│   ├── CreditMemoResponse.java    # Response DTOs
│   └── CreditMemoDocument.java    # Document structure
├── service/
│   ├── CreditMemoService.java     # Business logic & AI integration
│   └── CreditMemoGenerationException.java
└── exception/
    └── GlobalExceptionHandler.java # Error handling
```

## Spring AI Integration

The application uses Spring AI's `ChatClient` for interacting with AWS Bedrock:

```java
@Service
public class CreditMemoService {
    private final ChatClient chatClient;
    
    public CreditMemoResponse generateCreditMemo(CreditMemoRequest request) {
        CreditMemoDocument document = chatClient.prompt()
            .user(prompt)
            .call()
            .entity(CreditMemoDocument.class);
        // ...
    }
}
```

### Key Features Used:

1. **Structured Output**: Using `entity()` method to convert AI responses to Java records
2. **System Messages**: Pre-configured prompts for consistent behavior
3. **Chat Memory**: Support for conversational context (via starter dependency)
4. **Bedrock Converse**: Using the latest Bedrock Converse API

## Error Handling

The application includes comprehensive error handling:

- **Validation Errors**: 400 Bad Request with field details
- **Generation Failures**: 500 Internal Server Error with error details
- **Invalid Arguments**: 400 Bad Request with explanation
- **Unexpected Errors**: 500 Internal Server Error (generic message)

## Monitoring

Actuator endpoints are available at:

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Metrics: `http://localhost:8080/actuator/metrics`

## Security Considerations

1. **Authentication**: Add Spring Security for production use
2. **Authorization**: Implement role-based access control
3. **API Keys**: Secure sensitive endpoints
4. **Rate Limiting**: Prevent abuse of AI generation
5. **Audit Logging**: Track all credit memo generations
6. **PII Protection**: Handle customer data securely

## Future Enhancements

- [ ] Add persistence layer (database)
- [ ] Implement approval workflow
- [ ] Add PDF generation for credit memos
- [ ] Email notification system
- [ ] Audit trail and versioning
- [ ] Multi-language support
- [ ] Advanced analytics and reporting
- [ ] Integration with accounting systems

## Troubleshooting

### AWS Credentials Error
```
Error: Unable to load credentials from any of the providers
```
**Solution**: Ensure AWS credentials are properly configured (see Configuration section)

### Model Not Available
```
Error: Model not found
```
**Solution**: Ensure your AWS account has access to Claude models in Bedrock. Request access if needed.

### Connection Timeout
```
Error: Connection timeout
```
**Solution**: Check network connectivity and increase timeout in `application.yaml`

## License

This project is for demonstration purposes.

## Contact

For questions or support, contact the development team.
