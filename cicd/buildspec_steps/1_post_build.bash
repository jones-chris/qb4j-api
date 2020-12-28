#!/bin/bash

# Upload code coverage files.
#      - curl -s https://codecov.io/bash > codecov.sh
#      - bash codecov.sh -t $CODECOV_TOKEN

DOCKERHUB_TOKEN=$1
PROJECT_VERSION=$2
DOCKER_IMAGE_NAME="joneschris/qb4j-api:$PROJECT_VERSION"

echo "$DOCKERHUB_TOKEN" | docker login --username joneschris --password-stdin
docker push "$DOCKER_IMAGE_NAME"

echo "ENV environment variable is $ENV"

# If DEV env variable, then deploy to Lightsail.
# If PROD env variable, then deploy CloudFormation.
if [ "$ENV" == "swarm" ]; then
    echo "Deploying to swarm"

    chmod +x ./cicd/deployment/swarm_deployment.sh
    sh ./cicd/deployment/swarm_deployment.sh "$PROJECT_VERSION"
elif [ "$ENV" == "ecs" ]; then
    echo "Deploying to ECS"

    chmod +x ./cicd/deployment/ecs_deployment.sh
    sh ./cicd/deployment/ecs_deployment.sh "$DOCKER_IMAGE_NAME"
else
    echo "Did not recognize the ENV, $ENV.  Not deploying."
fi
