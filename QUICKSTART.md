# Quick Start Guide

## Prerequisites

1. **Java 21+** installed
2. **Maven 3.6+** installed
3. **AWS Account** with Bedrock access
4. **AWS Credentials** configured

## Setup (5 minutes)

### 1. Configure AWS Credentials

Choose one method:

**Option A: Environment Variables** (Recommended)
```bash
export AWS_ACCESS_KEY_ID=your_access_key_here
export AWS_SECRET_ACCESS_KEY=your_secret_key_here
export AWS_REGION=us-east-1
```

**Option B: AWS CLI**
```bash
aws configure
```

### 2. Enable Bedrock Models

1. Go to AWS Console → Bedrock
2. Navigate to "Model access"
3. Request access to Claude models (if not already enabled)
4. Wait for approval (usually instant)

### 3. Build and Run

```bash
# Navigate to project directory
cd /Users/alokkulkarni/Documents/Development/creditmemo

# Build the project
./mvnw clean package

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## First API Call

Once the application is running, test it:

```bash
# Health check
curl http://localhost:8080/api/v1/credit-memos/health

# Generate a credit memo
curl -X POST http://localhost:8080/api/v1/credit-memos \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-billing-error.json
```

Or use the test script:
```bash
./test-api.sh
```

## Expected Response

```json
{
  "creditMemoId": "uuid-here",
  "creditMemoNumber": "CM-2024-001",
  "generatedAt": "2024-11-22T10:30:00",
  "status": "PENDING_APPROVAL",
  "creditAmount": 500.00,
  "currency": "USD",
  "creditMemoDocument": "=== CREDIT MEMO ===\n...",
  "summary": "Credit memo issued to Acme Corporation for $500.00...",
  ...
}
```

## Project Files Overview

### Core Application Files
- `src/main/java/com/alok/ai/creditmemo/CreditmemoApplication.java` - Main application class
- `src/main/resources/application.yaml` - Configuration

### API Layer
- `controller/CreditMemoController.java` - REST endpoints
- `model/CreditMemoRequest.java` - Request DTOs
- `model/CreditMemoResponse.java` - Response DTOs
- `model/CreditMemoDocument.java` - Document structure

### Business Logic
- `service/CreditMemoService.java` - AI integration & business logic
- `service/CreditMemoGenerationException.java` - Custom exception

### Configuration
- `config/SpringAiConfig.java` - Spring AI setup
- `config/WebConfig.java` - CORS & web config
- `exception/GlobalExceptionHandler.java` - Error handling

### Documentation & Samples
- `README.md` - Full documentation
- `SPRING_AI_GUIDE.md` - Spring AI integration guide
- `samples/` - Sample request JSON files
- `test-api.sh` - API test script

## Common Issues

### Issue: "Model not found"
**Solution**: Ensure you've requested access to Claude models in AWS Bedrock console

### Issue: "Invalid credentials"
**Solution**: Verify AWS credentials are set correctly:
```bash
aws sts get-caller-identity
```

### Issue: "Connection timeout"
**Solution**: Check network connectivity to AWS. Try increasing timeout in `application.yaml`

### Issue: Port 8080 already in use
**Solution**: Change port in `application.yaml`:
```yaml
server:
  port: 8081
```

## Next Steps

1. **Explore API Endpoints**: Try all endpoints in `test-api.sh`
2. **Customize Prompts**: Modify prompts in `CreditMemoService.java`
3. **Add Authentication**: Implement Spring Security
4. **Add Persistence**: Integrate database (PostgreSQL, MongoDB, etc.)
5. **Enhance AI**: Experiment with different models and parameters
6. **Deploy**: Deploy to AWS ECS, Lambda, or EC2

## Development Tips

### Hot Reload
The project includes Spring DevTools for hot reload:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>
```

### Logging
Enable debug logging to see AI interactions:
```yaml
logging:
  level:
    org.springframework.ai: DEBUG
```

### Testing Different Models
Change model in `application.yaml`:
```yaml
spring:
  ai:
    bedrock:
      converse:
        chat:
          model: anthropic.claude-3-haiku-20240307-v1:0  # Faster, cheaper
```

## API Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/v1/credit-memos` | Generate credit memo |
| POST | `/api/v1/credit-memos/summary` | Generate summary only |
| POST | `/api/v1/credit-memos/validate` | Validate request |
| GET | `/api/v1/credit-memos/health` | Health check |

## Sample Requests

Three sample requests are provided:

1. **sample-request-billing-error.json** - Billing error correction ($500 credit)
2. **sample-request-product-return.json** - Product return ($3,600 credit)
3. **sample-request-service-issue.json** - Service quality issue ($7,500 credit)

Test them:
```bash
curl -X POST http://localhost:8080/api/v1/credit-memos \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-billing-error.json | jq .
```

## Cost Considerations

**Claude 3.5 Sonnet** (Default):
- ~$3 per 1M input tokens
- ~$15 per 1M output tokens
- Average credit memo: ~2,000 tokens = ~$0.04

**Claude 3 Haiku** (Faster, cheaper):
- ~$0.25 per 1M input tokens
- ~$1.25 per 1M output tokens
- Average credit memo: ~2,000 tokens = ~$0.003

For development/testing, consider using Claude 3 Haiku to reduce costs.

## Getting Help

- **Full Documentation**: See `README.md`
- **Spring AI Guide**: See `SPRING_AI_GUIDE.md`
- **Spring AI Docs**: https://docs.spring.io/spring-ai/reference/
- **AWS Bedrock Docs**: https://docs.aws.amazon.com/bedrock/

## Ready to Deploy?

Check the deployment section in `README.md` for:
- Docker containerization
- AWS deployment options (ECS, Lambda, EC2)
- Production considerations
- Security best practices

---

🚀 **You're ready to generate AI-powered credit memos!**
