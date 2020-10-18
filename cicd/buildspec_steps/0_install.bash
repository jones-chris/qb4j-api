#!/bin/bash

# Get information about this image, the current directory, maven version, docker version, then update apt-get.
lsb_release -a
pwd
mvn --version
docker --version
docker info
apt-get update
