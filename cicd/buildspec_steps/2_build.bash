#!/bin/bash

PROJECT_VERSION=$1
ENV=$2

# Build the uber jar and run unit tests.
mvn clean install

# Build docker image
docker image build -t joneschris/qb4j-api:"$PROJECT_VERSION" --build-arg environment="$ENV" --build-arg project_version="$PROJECT_VERSION" .
