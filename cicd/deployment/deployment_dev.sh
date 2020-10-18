#!/bin/bash

# This script should log into the lightsail instance using the SSH private key, stop the qb4j-api docker container, pull down the new image, start the qb4j-api docker container.
DOCKER_IMAGE_TAG=$1
$QB4J_CONFIG=$2

echo "Docker image tag / Project Version argument is: $1"
echo "QB4J_CONFIG is $QB4J_CONFIG"


# Get the private key, user name, and IP address for the lightsail instance to ssh into the lightsail instance.
PRIVATE_KEY=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/ssh_key --with-decryption --output text --query Parameter.Value)
USER_NAME=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/user_name --with-decryption --output text --query Parameter.Value)
IP_ADDRESS=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/ip_address --with-decryption --output text --query Parameter.Value)

# Put the private key in a txt file.
echo "$PRIVATE_KEY" > private_key.txt

chmod 600 private_key.txt

# ssh into the lightsail instance, pull the docker image, stop the existing docker container, start a container from the
# new image, sleep for 10 seconds (so the container can start so that the prune command can accurately tell which images
# are being used by containrs), and remove all unused images (so that the lightsail instance doesn't run out of disk space).
ssh -i private_key.txt -tt -o StrictHostKeyChecking=no "$USER_NAME@$IP_ADDRESS" bash -c 'sudo docker pull joneschris/qb4j-api:$DOCKER_IMAGE_TAG; DOCKER_CONTAINER_ID_TO_STOP=$(sudo docker ps | grep ''qb4j-api'' | awk ''{ print $1 }''); sudo docker container stop $DOCKER_CONTAINER_ID_TO_STOP; sudo nohup docker container run --publish 8080:8080 --detach --restart always --env qb4j_config="$(cat ./qb4j.yml)" joneschris/qb4j-api:$DOCKER_IMAGE_TAG &; sleep 10s; sudo docker image prune -a --force; exit;'
