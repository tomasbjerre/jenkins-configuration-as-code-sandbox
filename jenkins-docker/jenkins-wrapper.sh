#!/bin/bash

set -eux

shared_env_file=/shared/shared-env.txt

while [ ! -f $shared_env_file ]
do
  echo "waiting on $shared_env_file"
  sleep 2 # or less like 0.2
done
ls -l $shared_env_file

source $shared_env_file
export $(cut -d= -f1 $shared_env_file)


/usr/local/bin/jenkins.sh "$@"