#!/bin/bash

# This script should log into the lightsail instance using the SSH private key, stop the qb4j-api docker container, pull down the new image, start the qb4j-api docker container.
echo "Docker image tag / Project Version argument is: $1"

# Get the private key, user name, and IP address for the lightsail instance to ssh into the lightsail instance.
PRIVATE_KEY=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/ssh_key --with-decryption --output text --query Parameter.Value)
USER_NAME=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/user_name --with-decryption --output text --query Parameter.Value)
IP_ADDRESS=$(aws ssm get-parameter --name /dev/qb4j_api_lightsail/ip_address --with-decryption --output text --query Parameter.Value)

# Put the private key in a txt file.
echo "$PRIVATE_KEY" > private_key.txt

chmod 600 private_key.txt

# ssh into the lightsail instance, pull the docker image, stop the existing docker container, start a container from the
# new image.
ssh -i private_key.txt -tt -o StrictHostKeyChecking=no "$USER_NAME@$IP_ADDRESS" "/bin/bash -s $1" << "EOF"
  DOCKER_IMAGE_TAG=$1

  echo "DOCKER_IMAGE_TAG is $DOCKER_IMAGE_TAG"

  sudo docker pull joneschris/qb4j-mvc:"$DOCKER_IMAGE_TAG"

  DOCKER_CONTAINER_ID_TO_STOP=$(sudo docker ps | grep 'qb4j-mvc' | awk '{ print $1 }')

  sudo docker container stop "$DOCKER_CONTAINER_ID_TO_STOP"

  sudo nohup docker container run --publish 8080:8080 --detach joneschris/qb4j-mvc:"$DOCKER_IMAGE_TAG"

  exit
EOF
