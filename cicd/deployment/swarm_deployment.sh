#!/bin/bash

PROJECT_VERSION=$1

echo "Docker image tag / Project Version argument is: $PROJECT_VERSION"

QB_CONFIG=$(cat ./cicd/deployment/swarm/swarm-qb.yml)
echo "QB_CONFIG is $QB_CONFIG"

echo "DOCKER_SWARM_YAML is: "
cat ./cicd/deployment/swarm/docker-swarm.yml

scp ./cicd/deployment/swarm/docker-swarm.yml "$AWS_LIGHTSAIL_USERNAME@dev.api.querybuilder4j.net":/home/ubuntu/swarm/cicd/deployment/swarm
scp ./cicd/deployment/swarm/swarm-qb.yml "$AWS_LIGHTSAIL_USERNAME@dev.api.querybuilder4j.net":/home/ubuntu/swarm/cicd/deployment/swarm

ssh "$AWS_LIGHTSAIL_USERNAME@dev.api.querybuilder4j.net" /bin/bash << EOF
  export PROJECT_VERSION=$PROJECT_VERSION
  export UPDATE_CACHE=false
  export QB_CONFIG="$QB_CONFIG"
  sudo -E docker stack deploy --compose-file /home/ubuntu/swarm/cicd/deployment/swarm/docker-swarm.yml qb4j
  exit
EOF

