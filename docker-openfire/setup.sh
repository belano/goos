#!/bin/bash
set -e

error_exit() {
  echo "$1" 1>&2
  exit 1
}

COOKIES="/tmp/openfire-cookies"

# login and store cookies
URL_EFFECTIVE=$(curl -sSL -o /dev/null -w '%{url_effective}' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -H 'Cookie: csrf=h6uG55ROkOIPbp8' \
  --data-raw 'url=%2Findex.jsp&login=true&csrf=h6uG55ROkOIPbp8&username=admin&password=admin' \
  -c "$COOKIES" 'http://localhost:9090/login.jsp') ||
  error_exit "Login failed"
if [[ "$URL_EFFECTIVE" == "http://localhost:9090/index.jsp" ]]; then
  echo 'Successful login.'
else
  error_exit "Login failed, curl was redirected to $URL_EFFECTIVE"
fi

# enable rest api plugin
URL_EFFECTIVE=$(curl -sSL -o /dev/null -w '%{url_effective}' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw 'enabled=true' \
  -b "$COOKIES" 'http://localhost:9090/plugins/restapi/rest-api.jsp?save') ||
  error_exit "Enabling plugin failed"
if [[ "$URL_EFFECTIVE" == "http://localhost:9090/plugins/restapi/rest-api.jsp?success=true" ]]; then
  echo 'Plugin restapi enabled.'
else
  error_exit "Plugin restapi not enabled, curl was redirected to $URL_EFFECTIVE"
fi

CSRF=$(grep csrf $COOKIES | cut -f 7)
EXPECTED='Settings updated successfully.'

# resource policy
ACTUAL=$(curl -sS  \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw "csrf=$CSRF&kickPolicy=-1&kickValue=&update=Save+Settings" \
  -b "$COOKIES" 'http://localhost:9090/session-conflict.jsp' |
  grep "$EXPECTED" |
  awk '{$1=$1};1') ||
  error_exit "Updating resource policy failed"
if [[ "$ACTUAL" == "$EXPECTED" ]]; then
  echo "Conflict policy set to 'Never kick'"
else
  error_exit "Could not update conflict policy"
fi

# offline messages
ACTUAL=$(curl -sS \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw "csrf=$CSRF&storeStrategy=5&quota=100.00&strategy=2&update=Save+Settings" \
  -b "$COOKIES" 'http://localhost:9090/offline-messages.jsp' |
  grep "$EXPECTED" |
  awk '{$1=$1};1') ||
  error_exit "Updating offline message policy failed"
if [[ "$ACTUAL" == "$EXPECTED" ]]; then
  echo "Offline message policy set to 'Drop'"
else
  error_exit "Could not update offline message policy"
fi

create_user() {
  HTTP_CODE=$(curl -sS -o /dev/null -w '%{http_code}' \
    -X POST \
    -H 'Content-type: application/json' \
    -d '{
      "username": "'"$1"'",
      "password": "'"$2"'"
    }' --user admin:admin 'http://localhost:9090/plugins/restapi/v1/users') ||
    error_exit "Creating user failed"
  if [[ "$HTTP_CODE" == "201" ]]; then
    echo "Created user $1"
  else
    error_exit "Could not create user $1, curl received HTTP status $HTTP_CODE"
  fi
}

# create users
create_user "sniper" "sniper"
create_user "auction-item-54321" "auction"
create_user "auction-item-65432" "auction"
