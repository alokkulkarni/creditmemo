# Spring AI + AWS Bedrock Integration Guide

## Overview

This application uses **Spring AI 1.1.0** with **AWS Bedrock Converse API** to generate credit memos using Claude 3.5 Sonnet.

## Spring AI Architecture

```
Application Code
      ↓
ChatClient (High-level API)
      ↓
ChatModel Interface
      ↓
BedrockConverseChatModel (Implementation)
      ↓
AWS Bedrock Converse API
      ↓
Claude 3.5 Sonnet Model
```

## Key Spring AI Features Used

### 1. ChatClient Builder Pattern

The `ChatClient` provides a fluent API for interacting with LLMs:

```java
@Service
public class CreditMemoService {
    private final ChatClient chatClient;
    
    public CreditMemoService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
            .defaultSystem("You are a financial document specialist...")
            .build();
    }
}
```

### 2. Structured Output with `entity()`

Spring AI can automatically convert LLM responses to Java records:

```java
CreditMemoDocument document = chatClient.prompt()
    .user(prompt)
    .call()
    .entity(CreditMemoDocument.class);
```

This uses Jackson under the hood to deserialize the JSON response.

### 3. Prompt Templates

Build dynamic prompts using String templates:

```java
String prompt = String.format("""
    Generate a credit memo for:
    Customer: %s
    Amount: %s
    Reason: %s
    """, customerName, amount, reason);
```

### 4. Chat Memory Support

The application includes `spring-ai-starter-model-chat-memory` for conversational context:

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory</artifactId>
</dependency>
```

## AWS Bedrock Converse API

### Supported Models

Configure in `application.yaml`:

```yaml
spring:
  ai:
    bedrock:
      converse:
        chat:
          model: anthropic.claude-3-5-sonnet-20240620-v1:0
```

**Available Models:**
- `anthropic.claude-3-5-sonnet-20240620-v1:0` - Best for complex tasks
- `anthropic.claude-3-sonnet-20240229-v1:0` - Balanced performance
- `anthropic.claude-3-haiku-20240307-v1:0` - Fast, cost-effective
- `meta.llama3-1-405b-instruct-v1:0` - Alternative LLM

### Model Parameters

```yaml
spring:
  ai:
    bedrock:
      converse:
        chat:
          options:
            temperature: 0.7      # Creativity (0.0-1.0)
            max-tokens: 4096      # Maximum response length
            top-p: 0.9           # Nucleus sampling
```

**Parameter Guide:**
- **temperature**: Higher = more creative, Lower = more deterministic
  - 0.0-0.3: Factual, consistent
  - 0.4-0.7: Balanced (recommended for documents)
  - 0.8-1.0: Creative, varied

- **max-tokens**: Maximum response length
  - Credit memos: 2048-4096 tokens
  - Summaries: 512-1024 tokens

- **top-p**: Alternative to temperature
  - 0.9: Good default
  - Lower: More focused responses

## IAM Permissions Required

Your AWS credentials need these permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock:InvokeModel",
        "bedrock:InvokeModelWithResponseStream"
      ],
      "Resource": [
        "arn:aws:bedrock:*::foundation-model/anthropic.claude-*"
      ]
    }
  ]
}
```

## Error Handling

### Common Issues

1. **Model Not Available**
   ```
   ResourceNotFoundException: Could not resolve model
   ```
   **Solution**: Request model access in AWS Bedrock console

2. **Throttling**
   ```
   ThrottlingException: Rate exceeded
   ```
   **Solution**: Implement exponential backoff or request quota increase

3. **Invalid Credentials**
   ```
   UnauthorizedException: Invalid credentials
   ```
   **Solution**: Check AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY

### Spring AI Error Handling

```java
try {
    CreditMemoDocument doc = chatClient.prompt()
        .user(prompt)
        .call()
        .entity(CreditMemoDocument.class);
} catch (Exception e) {
    logger.error("AI generation failed", e);
    throw new CreditMemoGenerationException("Failed: " + e.getMessage(), e);
}
```

## Advanced Features

### 1. Streaming Responses

For long-running generations, use streaming:

```java
Flux<String> stream = chatClient.prompt()
    .user(prompt)
    .stream()
    .content();
    
stream.subscribe(
    chunk -> System.out.print(chunk),
    error -> logger.error("Error", error),
    () -> logger.info("Complete")
);
```

### 2. Function Calling

Define functions that the AI can call:

```java
@Bean
public FunctionCallback weatherFunction() {
    return FunctionCallback.builder()
        .function("getWeather", this::getWeather)
        .description("Get weather for a location")
        .build();
}
```

### 3. Custom Output Converters

For complex transformations:

```java
BeanOutputConverter<CreditMemoDocument> converter = 
    new BeanOutputConverter<>(CreditMemoDocument.class);

String format = converter.getFormat();
// Add format instructions to prompt

CreditMemoDocument doc = converter.convert(response);
```

## Performance Optimization

### 1. Connection Pooling

Configure timeout and connection settings:

```yaml
spring:
  ai:
    bedrock:
      aws:
        timeout: 60s
```

### 2. Caching

Cache common prompts and responses:

```java
@Cacheable("creditMemoTemplates")
public String getTemplate(String type) {
    // ...
}
```

### 3. Batch Processing

Process multiple requests efficiently:

```java
List<CompletableFuture<CreditMemoResponse>> futures = 
    requests.stream()
        .map(req -> CompletableFuture.supplyAsync(
            () -> generateCreditMemo(req)
        ))
        .toList();
```

## Monitoring and Observability

### 1. Enable Logging

```yaml
logging:
  level:
    org.springframework.ai: DEBUG
    com.amazonaws.bedrock: DEBUG
```

### 2. Track Metrics

Monitor:
- Request latency
- Token usage
- Error rates
- Model performance

### 3. Cost Tracking

Track costs by monitoring:
- Number of API calls
- Token usage (input + output)
- Model pricing (varies by model)

**Example Costs (as of 2024):**
- Claude 3.5 Sonnet: ~$3 per 1M input tokens, ~$15 per 1M output tokens
- Claude 3 Haiku: ~$0.25 per 1M input tokens, ~$1.25 per 1M output tokens

## Best Practices

1. **Use Specific Prompts**: Clear, detailed prompts yield better results
2. **Implement Retry Logic**: Handle transient failures gracefully
3. **Validate AI Output**: Always validate structured output
4. **Monitor Costs**: Track token usage and API calls
5. **Use Appropriate Models**: Choose based on complexity vs. cost
6. **Cache Results**: Cache common requests to reduce costs
7. **Rate Limiting**: Implement request throttling
8. **Error Boundaries**: Gracefully handle AI failures

## Testing with AWS Bedrock

### Local Testing

Use mock responses for development:

```java
@TestConfiguration
class MockChatModelConfig {
    @Bean
    @Primary
    public ChatModel mockChatModel() {
        return mock(ChatModel.class);
    }
}
```

### Integration Testing

Test with real Bedrock (use separate AWS account):

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.bedrock.aws.region=us-east-1"
})
class CreditMemoServiceIntegrationTest {
    // ...
}
```

## Migration from Legacy APIs

If migrating from older Bedrock APIs:

**Old (Bedrock Runtime)**:
```java
BedrockRuntimeClient client = BedrockRuntimeClient.create();
// Manual JSON handling
```

**New (Spring AI + Converse)**:
```java
ChatClient chatClient = ChatClient.builder(chatModel).build();
// Automatic serialization/deserialization
```

Benefits:
- Simplified API
- Automatic retry logic
- Built-in streaming support
- Type-safe structured output
- Unified interface across providers

## Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)
- [Claude API Documentation](https://docs.anthropic.com/claude/reference/)
- [Spring AI GitHub](https://github.com/spring-projects/spring-ai)

## Support

For issues specific to:
- **Spring AI**: Check GitHub issues
- **AWS Bedrock**: Contact AWS Support
- **This Application**: Check application logs and README
