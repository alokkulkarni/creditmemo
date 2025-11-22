#!/bin/bash

# Credit Memo API Test Script
# This script tests all endpoints of the Credit Memo Generation API

BASE_URL="http://localhost:8080"
API_PATH="/api/v1/credit-memos"

echo "============================================"
echo "Credit Memo API Test Suite"
echo "============================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Health Check
echo -e "${YELLOW}Test 1: Health Check${NC}"
echo "GET ${BASE_URL}${API_PATH}/health"
curl -s -X GET "${BASE_URL}${API_PATH}/health" | jq .
echo ""
echo "---"
echo ""

# Test 2: Generate Credit Memo (Billing Error)
echo -e "${YELLOW}Test 2: Generate Credit Memo - Billing Error${NC}"
echo "POST ${BASE_URL}${API_PATH}"
curl -s -X POST "${BASE_URL}${API_PATH}" \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-billing-error.json | jq .
echo ""
echo "---"
echo ""

# Test 3: Generate Summary
echo -e "${YELLOW}Test 3: Generate Credit Memo Summary${NC}"
echo "POST ${BASE_URL}${API_PATH}/summary"
curl -s -X POST "${BASE_URL}${API_PATH}/summary" \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-product-return.json | jq .
echo ""
echo "---"
echo ""

# Test 4: Validate Request
echo -e "${YELLOW}Test 4: Validate Credit Memo Request${NC}"
echo "POST ${BASE_URL}${API_PATH}/validate"
curl -s -X POST "${BASE_URL}${API_PATH}/validate" \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-service-issue.json | jq .
echo ""
echo "---"
echo ""

# Test 5: Generate Credit Memo (Product Return)
echo -e "${YELLOW}Test 5: Generate Credit Memo - Product Return${NC}"
echo "POST ${BASE_URL}${API_PATH}"
curl -s -X POST "${BASE_URL}${API_PATH}" \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-product-return.json | jq .
echo ""
echo "---"
echo ""

# Test 6: Invalid Request (should fail validation)
echo -e "${YELLOW}Test 6: Invalid Request (Empty Body)${NC}"
echo "POST ${BASE_URL}${API_PATH}"
curl -s -X POST "${BASE_URL}${API_PATH}" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .
echo ""
echo "---"
echo ""

echo -e "${GREEN}Test suite completed!${NC}"
echo ""
echo "To run individual tests:"
echo "  curl -X GET ${BASE_URL}${API_PATH}/health"
echo "  curl -X POST ${BASE_URL}${API_PATH} -H 'Content-Type: application/json' -d @samples/sample-request-billing-error.json"
