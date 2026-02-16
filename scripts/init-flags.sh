#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}Initializing feature flags in Unleash...${NC}"

echo "Waiting for Unleash to be ready..."
until curl -s http://localhost:4242/health > /dev/null; do
    sleep 2
done
echo -e "${GREEN}Unleash is ready!${NC}"

API_TOKEN="*:*.unleash-default-token"
UNLEASH_URL="http://localhost:4242/api"

create_feature_flag() {
    local flag_name=$1
    local flag_description=$2
    local flag_type=$3

    echo -e "${YELLOW}Creating feature flag: $flag_name${NC}"

    curl -s -X POST "$UNLEASH_URL/admin/projects/default/features" \
        -H "Authorization: $API_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{
            \"name\": \"$flag_name\",
            \"description\": \"$flag_description\",
            \"type\": \"$flag_type\",
            \"impressionData\": false
        }"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Created $flag_name${NC}"
    else
        echo -e "${RED}✗ Failed to create $flag_name${NC}"
    fi
}

enable_feature_flag() {
    local flag_name=$1

    echo -e "${YELLOW}Enabling feature flag: $flag_name${NC}"

    curl -s -X POST "$UNLEASH_URL/admin/projects/default/features/$flag_name/environments/development/on" \
        -H "Authorization: $API_TOKEN" \
        -H "Content-Type: application/json"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Enabled $flag_name${NC}"
    else
        echo -e "${RED}✗ Failed to enable $flag_name${NC}"
    fi
}

create_feature_flag "premium-pricing" "Enable premium pricing discounts for eligible users" "release"
create_feature_flag "order-notifications" "Send notifications when orders are created" "release"
create_feature_flag "bulk-order-discount" "Apply 15% discount for orders with quantity > 5" "release"

enable_feature_flag "premium-pricing"
enable_feature_flag "order-notifications"
enable_feature_flag "bulk-order-discount"

echo -e "${GREEN}Feature flag initialization complete!${NC}"
echo -e "${YELLOW}You can view flags at: http://localhost:4242${NC}"