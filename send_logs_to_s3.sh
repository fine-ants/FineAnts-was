#!/bin/bash

# 설정값
LOG_DIR="${PWD}/logs" # 로그 파일이 저장된 디렉토리
BACKUP_DIR="${PWD}/backup" # 압축된 파일을 저장할 임시 디렉토리
S3_BUCKET="s3://fineants2024/production/logs" # S3 버킷 경로
RETENTION_DAYS=7 # 로컬에서 유지할 로그 파일의 보관 기간

# 날짜 및 인스턴스 정보 가져오기
YEAR_PATH="year=$(date -d "yesterday" +"%Y")"
MONTH_PATH="month=$(date -d "yesterday" +"%m")"
DAY_PATH="day=$(date -d "yesterday" +"%d")"
HOSTNAME=$(hostname) # EC2 인스턴스의 호스트 이름 가져오기
INSTANCE_ID=$(aws ec2 describe-instances --query "Reservations[0].Instances[0].InstanceId" --output text)

# 설정값 출력
echo "LOG_DIR=$LOG_DIR"
echo "BACKUP_DIR=$BACKUP_DIR"
echo "S3_BUCKET=$S3_BUCKET"
echo "RETENTION_DAYS=$RETENTION_DAYS"
echo "YEAR=$YEAR_PATH"
echo "MONTH=$MONTH_PATH"
echo "DAY=$DAY_PATH"
echo "HOSTNAME=$HOSTNAME"
echo "INSTANCE_ID=$INSTANCE_ID"

# 백업 디렉토리 생성 (없으면 생성)
mkdir -p "$BACKUP_DIR"

# 어제 로그 파일 압축
echo "Compressing log files..."
sudo find "$LOG_DIR" -type f -name "spring*.log" -mtime -1 -exec sh -c 'gzip -c "$1" > "$1.gz"' _ {} \;

# 압축한 파일을 백업 디렉토리로 이동
echo "move the gzip file to the $BACKUP_DIR"
sudo find "$LOG_DIR" -type f -name "spring*.log.gz" -exec mv {} "$BACKUP_DIR/" \;

# S3로 업로드
echo "Uploading to S3..."
sudo aws s3 cp "$BACKUP_DIR/" "$S3_BUCKET/$YEAR_PATH/$MONTH_PATH/$DAY_PATH/$INSTANCE_ID/" --recursive

# 업로드 완료 후 로컬 백업 파일 정리
echo "Cleaning up local back files..."
sudo find "$BACKUP_DIR" -type f -name "*.gz" -exec rm -f {} \;

# 오래된 로그 삭제(RETENTION_DYAS 이전 파일 삭제)
echo "Removing old logs..."
sudo find "$LOG_DIR" -type f -mtime +$RETENTION_DAYS -exec rm -f {} \;

echo "Backup and upload completed!"

