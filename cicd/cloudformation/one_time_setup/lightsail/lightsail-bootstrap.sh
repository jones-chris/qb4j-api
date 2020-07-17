#!/bin/bash

# This script is meant to be a bootstrap

DOCKER_USERNAME=$1
DOCKER_PASSWORD=$2
DOCKER_IMAGE_TAG=$3 # Hard code this to `dev`?

# Forward traffic from port 80 to 8080 where the Spring app is running.
sudo iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080

# Create an /apps directory.
mkdir apps && cd apps

# Create a /qb4j directory inside the /apps directory.
mkdir qb4j && cd qb4j

# Download docker-compose binary.
sudo curl -L "https://github.com/docker/compose/releases/download/1.26.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Give the docker-compose binary executable permission.
sudo chmod +x /usr/local/bin/docker-compose

# Install docker
# https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-18-04
sudo apt-get remove docker docker-engine docker.io containerd runc

sudo apt-get update

sudo apt install apt-transport-https ca-certificates curl software-properties-common -y

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"

sudo apt update

apt-cache policy docker-ce

sudo apt install docker-ce -y

# Docker login.
sudo docker login --username "$DOCKER_USERNAME" --password "$DOCKER_PASSWORD"

# Pull docker image.
sudo docker pull joneschris/qb4j-mvc:"$DOCKER_IMAGE_TAG"

# Run docker image in a container
sudo nohup docker container run --publish 8080:8080 --detach joneschris/qb4j-mvc:$"DOCKER_IMAGE_TAG"
