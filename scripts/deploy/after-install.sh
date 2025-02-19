#!/bin/bash
# scripts/after-install.sh

BASE_DIR="/home/ec2-user/build"
TEMP_DIR="$BASE_DIR/temp"
DEPLOY_DIR="$BASE_DIR/$DEPLOYMENT_GROUP_NAME"

# 디렉토리 존재 여부 확인 후 생성
mkdir -p "$DEPLOY_DIR"
# temp의 모든 파일을 해당 디렉토리로 복사
cp -R "$TEMP_DIR"/* "$DEPLOY_DIR"
# 임시 디렉토리 삭제
sudo rm -rf /home/ec2-user/build/temp
