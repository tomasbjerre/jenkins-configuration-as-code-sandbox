#!/bin/bash

source config.txt
export $(cut -d= -f1 config.txt)
token=$(cat personal-access-token.txt)
export sshKey=$(cat ~/.ssh/id_rsa.pub)

cat $0.json | envsubst > tmp.json
curl -v \
 -H "Content-Type: application/json" \
 -H "PRIVATE-TOKEN: $token" \
 -d @tmp.json \
 $gitlab_host/api/v4/users/1/keys
