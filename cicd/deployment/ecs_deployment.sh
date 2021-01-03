#!/bin/bash

## Find all the cloudformation templates.
#find ./cicd/cloudformation/stacks -name "*.cloudformation.yaml" > ./cf_stacks.txt
#
#while read LINE; do
#
#  # Get file name.
#  FILE_NAME=$(basename "$LINE")
#
#  # Get the stack name.
#  NAME=$(echo "$FILE_NAME" | awk -F . '{print $2}')
#
#  echo "Deploying $FILE_NAME with stack name, $NAME"
#
#  # Deploy the cloudformation template.
#  aws cloudformation deploy --template-file "$LINE" --stack-name "$NAME" --capabilities CAPABILITY_NAMED_IAM
#
#done < ./cf_stacks.txt

DOCKER_IMAGE_NAME=$1

aws cloudformation deploy \
    --template-file ./cicd/cloudformation/stacks/qb4j-api-ecs-service.cloudformation.yaml \
    --stack-name qb4j-api-ecs-service \
    --capabilities CAPABILITY_NAMED_IAM \
    --parameter-overrides DockerImage="$DOCKER_IMAGE_NAME"
