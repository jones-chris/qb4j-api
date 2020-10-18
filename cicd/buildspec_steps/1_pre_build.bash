#!/bin/bash

# Get project's version from pom.xml.
PROJECT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

# Check if the image exists in docker hub already.  If the image tag matches the project's version from the POM,
# then exit/fail the build.
IMAGE_EXISTS=$(docker pull joneschris/qb4j-api:"$PROJECT_VERSION" > /dev/null && echo "true" || echo "false")
echo "Does the image exist?  $IMAGE_EXISTS"

if [ "$IMAGE_EXISTS" == "true" ]; then
  echo "The image, $PROJECT_VERSION, already exists.  You probably need to bump the project's version in the pom.xml."
  exit 1
fi

# Copy custom maven settings from S3 bucket.
aws s3 cp s3://maven-build-settings/qb4j_settings.xml /root/.m2/settings.xml
