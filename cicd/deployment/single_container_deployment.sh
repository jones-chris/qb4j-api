#!/bin/bash

# This script should log into the lightsail instance using the SSH private key, stop the qb4j-api docker container, pull down the new image, start the qb4j-api docker container.
PROJECT_VERSION=$1

echo "Docker image tag / Project Version argument is: $PROJECT_VERSION"

# Put the private key in a txt file.
echo "$AWS_LIGHTSAIL_SSH_KEY" > private_key.txt
chmod 600 private_key.txt

# ssh into the lightsail instance, stop all containers, remove all containers, and remove all images.  This creates a
# clean slate for the next command to pull the docker image and start a container with that docker image.
# NOTE:  These steps are broken into 2 ssh commands because the first command needs to be expanded on the server side - thus
#        EOF is wrapped in double quotes.  Whereas the second ssh command needs to be expanded on the client side - thus
#        EOF is NOT wrapped in double quotes.  I am not aware of a way to accomplish both server and client side expansion
#        in the same ssh command.
ssh -i private_key.txt -tt -o StrictHostKeyChecking=no "$AWS_LIGHTSAIL_USERNAME@$AWS_LIGHTSAIL_IP_ADDRESS" /bin/bash << "EOF"
  sudo docker stop $(sudo docker ps -aq)
  sudo docker rm $(sudo docker ps -aq)
  sudo docker rmi $(sudo docker images -a -q)
  exit
EOF

# ssh into the lightsail instance, pull the docker image, and start a container based on the image.
# shellcheck disable=SC2087
#ssh -i private_key.txt -tt -o StrictHostKeyChecking=no "$USER_NAME@$IP_ADDRESS" /bin/bash << EOF
#  sudo docker pull joneschris/qb4j-api:$DOCKER_IMAGE_TAG
#  sudo nohup docker container run --publish 8080:8080 --detach --restart always --env qb4j_config="$QB4J_CONFIG" joneschris/qb4j-api:$DOCKER_IMAGE_TAG &
#  exit
#EOF