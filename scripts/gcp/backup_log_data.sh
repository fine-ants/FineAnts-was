#!/bin/bash

# .env 파일이 존재하는 경우 환경 변수로 로드
if [ -f "$ENV_FILE" ]; then
    # shellcheck disable=SC1090
    source "$ENV_FILE"
else
    echo "Error: .env.production file not found at $ENV_FILE"
    exit 1
fi

# 날짜 및 인스턴스 정보 가져오기
YEAR_PATH="year=$(date -d "yesterday" +"%Y")"
MONTH_PATH="month=$(date -d "yesterday" +"%m")"
DAY_PATH="day=$(date -d "yesterday" +"%d")"

# 설정값 출력
echo "LOG_DIR=$LOG_DIR" >> "$DEPLOY_FILE"
echo "BACKUP_DIR=$BACKUP_DIR" >> "$DEPLOY_FILE"
echo "BUCKET_LOG_PATH=$BUCKET_LOG_PATH" >> "$DEPLOY_FILE"
echo "RETENTION_DAYS=$RETENTION_DAYS" >> "$DEPLOY_FILE"
echo "YEAR=$YEAR_PATH" >> "$DEPLOY_FILE"
echo "MONTH=$MONTH_PATH" >> "$DEPLOY_FILE"
echo "DAY=$DAY_PATH" >> "$DEPLOY_FILE"
echo "DEPLOY_FILE=$DEPLOY_FILE" >> "$DEPLOY_FILE"

# 백업 디렉토리 생성 (없으면 생성)
mkdir -p "$BACKUP_DIR"

# 어제 로그 파일 압축
echo "Compressing log files..." >> "$DEPLOY_FILE"
sudo find "$LOG_DIR" -type f -name "spring*.log" -mtime -1 -exec sh -c 'gzip -c "$1" > "$1.gz"' _ {} \;

# 압축한 파일을 백업 디렉토리로 이동
echo "move the gzip file to the $BACKUP_DIR" >> "$DEPLOY_FILE"
sudo find "$LOG_DIR" -type f -name "spring*.log.gz" -exec mv {} "$BACKUP_DIR/" \;

# Bucket 업로드
echo "Uploading to Bucket..." >> "$DEPLOY_FILE"
sudo gsutil cp -r "$BACKUP_DIR/" "$BUCKET_LOG_PATH/$YEAR_PATH/$MONTH_PATH/$DAY_PATH/"

# 업로드 완료 후 로컬 백업 파일 정리
echo "Cleaning up local back files..." >> "$DEPLOY_FILE"
sudo find "$BACKUP_DIR" -type f -name "*.gz" -exec rm -f {} \;

# 오래된 로그 삭제(RETENTION_DYAS 이전 파일 삭제)
echo "Removing old logs..." >> "$DEPLOY_FILE"
# shellcheck disable=SC2086
sudo find "$LOG_DIR" -type f -mtime +$RETENTION_DAYS -exec rm -f {} \;

echo "Backup and upload completed!" >> "$DEPLOY_FILE"

