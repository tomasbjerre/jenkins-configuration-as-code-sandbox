#!/bin/bash

source config.txt
export $(cut -d= -f1 config.txt)
token=$(cat personal-access-token.txt)

for i in `seq 1 20`; do
  export projectName="project${i}name"
  cat $0.json | envsubst > tmp.json
  curl -v \
  -H "Content-Type: application/json" \
  -H "PRIVATE-TOKEN: $token" \
  -d @tmp.json \
  $gitlab_host/api/v4/projects

  cd project1/git-repo
  rm -rf .git
  git init
  git add .
  git commit -a -m first
  git remote add origin http://$gitlab_user:$gitlab_password@localhost/root/${projectName}.git
  git push -u origin master
  cd -
done