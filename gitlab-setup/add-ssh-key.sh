#!/bin/bash

set -eux

source config.txt
export $(cut -d= -f1 config.txt)
token=$(cat /shared/personal-access-token.txt)
export sshKey=$(cat ~/.ssh/id_rsa.pub)

cat $0.json | envsubst > tmp.json
curl -v \
 -H "Content-Type: application/json" \
 -H "PRIVATE-TOKEN: $token" \
 -d @tmp.json \
 $gitlab_host/api/v4/users/1/keys
