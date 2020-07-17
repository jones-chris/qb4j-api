#!/bin/bash

# Upload code coverage files.
#      - curl -s https://codecov.io/bash > codecov.sh
#      - bash codecov.sh -t $CODECOV_TOKEN

DOCKERHUB_TOKEN=$1
PROJECT_VERSION=$2

echo "$DOCKERHUB_TOKEN" | docker login --username joneschris --password-stdin
docker push joneschris/qb4j-mvc:"$PROJECT_VERSION"

echo "ENV environment variable is $ENV"

# If DEV env variable, then deploy to Lightsail.
# If PROD env variable, then deploy CloudFormation.
if [ "$ENV" == "dev" ]; then
    echo "Deploying to DEV"
    chmod +x ./cicd/deployment/deployment_dev.sh
    sh ./cicd/deployment/deployment_dev.sh "$PROJECT_VERSION"
elif [ "$ENV" == "prod" ]; then
    echo "Deploying to PROD"
    chmod +x ./cicd/deployment/deployment_prod.sh
    sh ./cicd/deployment/deployment_prod.sh
else
    echo "Did not recognize the ENV, $ENV.  Not deploying."
fi
