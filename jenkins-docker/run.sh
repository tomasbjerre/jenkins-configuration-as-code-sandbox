#!/bin/bash
export personal_access_token=$(cat ../gitlab-docker/setup/personal-access-token.txt)
echo --------------------------------------------------------
echo -
echo -  Using GitLab personal access token:
echo -
echo -    $personal_access_token
echo -
echo --------------------------------------------------------

docker run \
 --name jenkins-sandbox \
 -p 8080:8080 \
 -e personal_access_token=$personal_access_token \
 docker-jenkins-sandbox
