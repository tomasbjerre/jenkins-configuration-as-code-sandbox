#!/bin/bash

cd gitlab-docker/setup
./create-personal-access-token.sh
./global-settings.sh
./add-ssh-key.sh
./create-projects.sh
cd -
