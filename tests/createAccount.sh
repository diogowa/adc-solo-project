#!/bin/bash

# Base URL
BASE_URL="http://localhost:8080/rest"

# Function to execute POST requests
call_endpoint() {
  NAME=$1
  URL=$2
  DATA=$3

  echo "=============================="
  echo "Running test: $NAME"
  echo "POST $URL"

  HTTP_CODE=$(curl -s -o /tmp/body.json -w "%{http_code}" -X POST "$URL" \
    -H "Content-Type: application/json" \
    -d "$DATA")

  BODY=$(cat /tmp/body.json)

  echo "Status: $HTTP_CODE"
  echo "Response: $BODY"

  # Automatic assert on response code
  if [ "$HTTP_CODE" = "200" ]; then
    echo "  ✅ HTTP 200"
  else
    echo "  ❌ HTTP — expected: 200, received: $HTTP_CODE"
  fi

  echo ""
  echo "$BODY" # return to who called
}

# Function to assert
assert_field() {
  RESPONSE=$1
  FIELD=$2
  EXPECTED=$3

  ACTUAL=$(echo "$RESPONSE" | jq -r "$FIELD // empty")

  if [ "$ACTUAL" = "$EXPECTED" ]; then
    echo "  ✅ $FIELD = \"$EXPECTED\""
  else
    echo "  ❌ $FIELD — expected: \"$EXPECTED\", received: \"$ACTUAL\""
  fi
}

# --- Test Cases ---
RESP=$(call_endpoint "Create User - username is null" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - invalid username" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - password is null" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - confirmation is null" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"",
      "phone":"1234",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - password and confirmation does not match" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"asd",
      "phone":"1234",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - phone is null" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - invalid phone (not minimum digits)" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"12",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - invalid phone (is not number)" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"abcdefg",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - address is null" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - role is null" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"street",
      "role":""
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User - invalid role" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"street",
      "role":"NONE"
    }
  }')
assert_field "$RESP" ".status" "9906"

RESP=$(call_endpoint "Create User" \
  "$BASE_URL/createaccount" \
  '{
    "input": {
      "username":"user1@fct",
      "password":"pwd",
      "confirmation":"pwd",
      "phone":"1234",
      "address":"street",
      "role":"USER"
    }
  }')
assert_field "$RESP" ".status" "success"
assert_field "$RESP" ".data.username" "user1@fct"
assert_field "$RESP" ".data.role" "USER"

echo "All tests completed."
