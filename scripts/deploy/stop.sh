#!/bin/bash
# scripts/stop.sh

BASE_DIR="/home/ec2-user/build"
DOCKER_COMPOSE_FILE="docker-compose.${DEPLOYMENT_GROUP_NAME}.yml"
DEPLOY_DIR="$BASE_DIR/$DEPLOYMENT_GROUP_NAME"

if [ -f "$DEPLOY_DIR/$DOCKER_COMPOSE_FILE" ]; then
    cd "$DEPLOY_DIR" || exit
    sudo docker-compose -f "$DEPLOY_DIR/$DOCKER_COMPOSE_FILE" down
else
    echo "$DOCKER_COMPOSE_FILE 파일이 존재하지 않습니다."
fi
