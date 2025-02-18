#!/bin/bash

if [ "$DEPLOYMENT_GROUP_NAME" == "release" ]
then
    mkdir -p /home/ec2-user/build/release  # 디렉토리 존재 여부 확인 후 생성
    cp -R /home/ec2-user/build/temp/* /home/ec2-user/build/release
fi

if [ "$DEPLOYMENT_GROUP_NAME" == "production" ]
then
    mkdir -p /home/ec2-user/build/production  # 디렉토리 존재 여부 확인 후 생성
    cp -R /home/ec2-user/build/temp/* /home/ec2-user/build/production
fi
# Clean Up
sudo rm -rf /home/ec2-user/build/temp
