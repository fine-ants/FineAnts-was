#!/bin/bash
# scripts/start.sh

# 배포 환경을 PROFILE로 설정
PROFILE="$DEPLOYMENT_GROUP_NAME"
BASE_DIR="/home/ec2-user/build"
DEPLOY_DIR="$BASE_DIR/$PROFILE"
DOCKER_COMPOSE_FILE="$DEPLOY_DIR/docker-compose.$PROFILE.yml"
CRON_SETUP_SCRIPT="$DEPLOY_DIR/scripts/setup_cron.sh"

# 해당 배포 환경 디렉토리로 이동
cd "$DEPLOY_DIR" || exit

# Docker와 Docker Compose 실행 권한 설정
sudo chmod 666 /var/run/docker.sock
sudo chmod +x /usr/local/bin/docker-compose

# prometheus 실행 권한 설정
sudo chown -R "$(whoami):$(whoami)" "$PROFILE/prometheus"
sudo chmod -R 777 "$PROFILE/prometheus"

# docker-compose 실행
sudo docker-compose -f "$DOCKER_COMPOSE_FILE" pull
sudo docker-compose -f "$DOCKER_COMPOSE_FILE" up -d

# cron job 설정 스크립트 실행
echo ">>> Setting up cron job" >> "$DEPLOY_DIR/deploy.log"
sudo chmod +x "$CRON_SETUP_SCRIPT"
sudo bash "$CRON_SETUP_SCRIPT"
