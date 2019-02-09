#!/bin/bash
docker pull jenkins/jenkins:lts
docker rmi -f docker-jenkins-sandbox
docker rm -f jenkins-sandbox
docker build . \
 -t docker-jenkins-sandbox

