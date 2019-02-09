#!/bin/bash

source config.txt
export $(cut -d= -f1 config.txt)
token=$(cat personal-access-token.txt)

curl -v \
 -H "Content-Type: application/json" \
 -H "PRIVATE-TOKEN: $token" \
 --request PUT \
 $gitlab_host/api/v4/application/settings?auto_devops_enabled=false

curl -v \
 -H "Content-Type: application/json" \
 -H "PRIVATE-TOKEN: $token" \
 --request PUT \
 $gitlab_host/api/v4/application/settings?allow_local_requests_from_hooks_and_services=true
