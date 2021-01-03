#!/bin/bash

PROJECT_VERSION=$1

aws s3 cp "./target/qb4j-api-$PROJECT_VERSION.jar" "s3://$AWS_QB4J_API_BUCKET/travis-builds/"
