#!/bin/bash

# shellcheck disable=SC2034
PROFILE="$DEPLOYMENT_GROUP_NAME"

cd /home/ec2-user/build/"$PROFILE" || exit

BUILD_JAR=$(ls /home/ec2-user/build/"$PROFILE"/build/libs/*.jar)
JAR_NAME=$(basename $BUILD_JAR)
echo ">>> build filename: $JAR_NAME" >> /home/ec2-user/build/"$PROFILE"/deploy.log

echo ">>> copy build file" >> /home/ec2-user/build/"$PROFILE"/deploy.log
DEPLOY_PATH=/home/ec2-user/build/"$PROFILE"/
cp $BUILD_JAR $DEPLOY_PATH

sudo chmod 666 /var/run/docker.sock
sudo chmod +x /usr/local/bin/docker-compose
sudo docker-compose -f /home/ec2-user/build/"$PROFILE"/docker-compose-"$PROFILE".yml build
sudo docker-compose -f /home/ec2-user/build/"$PROFILE"/docker-compose-"$PROFILE".yml pull
sudo docker-compose -f /home/ec2-user/build/"$PROFILE"/docker-compose-"$PROFILE".yml up -d

# 실행하고 싶은 setup_cron.sh 파일 실행
echo ">>> Setting up cron job" >> /home/ec2-user/build/"$PROFILE"/deploy.log
sudo chmod +x /home/ec2-user/build/"$PROFILE"/setup_cron.sh
sudo bash /home/ec2-user/build/"$PROFILE"/setup_cron.sh
