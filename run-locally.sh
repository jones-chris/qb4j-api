#!/bin/bash

export PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

mvn clean install -Dmaven.javadoc.skip=true &&
    sudo docker image build -t joneschris/qb:"$PROJECT_VERSION" --build-arg project_version="$PROJECT_VERSION" .

export QB_CONFIG=$(cat ./.docker-compose/compose-qb.yml)

export UPDATE_CACHE=false

sudo -E docker-compose -f ./.docker-compose/docker-compose.yml up &&
    sudo -E docker-compose -f ./.docker-compose/docker-compose.yml down
