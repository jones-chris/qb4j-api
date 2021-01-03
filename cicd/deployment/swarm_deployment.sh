#!/bin/bash

# This script should log into the lightsail instance using the SSH private key, stop the qb4j-api docker container, pull down the new image, start the qb4j-api docker container.
PROJECT_VERSION=$1

echo "Docker image tag / Project Version argument is: $PROJECT_VERSION"
QB4J_CONFIG=$(cat ./cicd/deployment/swarm/swarm-qb4j.yml)
echo "QB4J_CONFIG is $QB4J_CONFIG"

# Put the private key in a txt file.
#echo "$AWS_LIGHTSAIL_SSH_KEY" > private_key.txt
#chmod 600 private_key.txt

# ssh into the lightsail instance, stop all containers, remove all containers, and remove all images.  This creates a
# clean slate for the next command to pull the docker image and start a container with that docker image.
# NOTE:  These steps are broken into 2 ssh commands because the first command needs to be expanded on the server side - thus
#        EOF is wrapped in double quotes.  Whereas the second ssh command needs to be expanded on the client side - thus
#        EOF is NOT wrapped in double quotes.  I am not aware of a way to accomplish both server and client side expansion
#        in the same ssh command.
DOCKER_SWARM_YAML=$(cat ./cicd/deployment/swarm/docker-swarm.yml)
echo "DOCKER_SWARM_YAML is: "
echo "$DOCKER_SWARM_YAML"

ssh -tt -o StrictHostKeyChecking=no "$AWS_LIGHTSAIL_USERNAME@dev.api.querybuilder4j.net" /bin/bash << "EOF"
  export PROJECT_VERSION=$PROJECT_VERSION
  export UPDATE_CACHE=false
  export QB4J_CONFIG="$QB4J_CONFIG"
  "$DOCKER_SWARM_YAML" | sudo -E docker stack deploy --compose-file - qb4j
  exit
EOF
