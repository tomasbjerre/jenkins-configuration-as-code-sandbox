#!/bin/bash

set -eux

cd "$(dirname "$0")"

source config.txt
export $(cut -d= -f1 config.txt)

token=$(./personal_access_token.py)

echo "$token" > /shared/personal-access-token.txt
echo "personal_access_token=$token" > /shared/shared-env.txt
./global-settings.sh
./add-ssh-key.sh
./create-projects.sh

