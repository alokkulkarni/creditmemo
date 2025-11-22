#!/bin/bash

# Test 7: External Bank Scenario - Business Customer with Recipient at Different Bank

echo "=========================================="
echo "Test 7: External Bank - Business Customer"
echo "=========================================="
echo "Expected: Issuer=RetailCorp Ltd (UK Business Bank), Recipient=Northern Supplies Limited (Barclays)"
echo ""

curl -s -X POST http://localhost:8080/api/credit-memos \
  -H "Content-Type: application/json" \
  -d @samples/sample-request-external-bank-customer.json \
  | jq -r '
    "MEMO NUMBER: " + .memoNumber,
    "ISSUE DATE: " + .issueDate,
    "",
    "ISSUER (FROM):",
    "  Company: " + .issuer.companyName,
    "  Address: " + .issuer.address.street + ", " + .issuer.address.city + ", " + .issuer.address.zipCode,
    "  Account: " + .issuer.accountNumber,
    (if .issuer.bankDetails then
      "  Bank: " + .issuer.bankDetails.bankName,
      "  Sort Code: " + .issuer.bankDetails.sortCode,
      "  SWIFT: " + .issuer.bankDetails.swiftCode
    else "" end),
    "",
    "RECIPIENT (TO):",
    "  Company: " + .recipient.companyName,
    "  Address: " + .recipient.address.street + ", " + .recipient.address.city + ", " + .recipient.address.zipCode,
    "  Account: " + .recipient.accountNumber,
    (if .recipient.bankDetails then
      "  Bank: " + .recipient.bankDetails.bankName,
      "  Sort Code: " + .recipient.bankDetails.sortCode,
      "  SWIFT: " + .recipient.bankDetails.swiftCode
    else "" end),
    "",
    "CREDIT REASON: " + .creditReason,
    "CREDIT AMOUNT: £" + (.creditAmount|tostring)
  '

echo ""
echo "=========================================="
