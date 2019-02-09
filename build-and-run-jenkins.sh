#!/bin/bash

cd jenkins-docker

./build.sh \
 && ./run.sh

cd -
