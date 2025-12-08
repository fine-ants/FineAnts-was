#!/bin/bash
# backup_mysql_data.sh

# .env 파일이 존재하는 경우 환경 변수로 로드
if [ -f "$ENV_FILE" ]; then
    # shellcheck disable=SC1090
    source "$ENV_FILE"
else
    echo "Error: .env.production file not found at $ENV_FILE"
    exit 1
fi

# 백업 대상 디렉토리 (ENV_FILE에서 로드)
BACKUP_DIR="$MYSQL_DATA_DIR"
# 백업 파일 경로 (ENV_FILE에서 로드)
BACKUP_FILE="$BACKUP_FILE_PATH"
# GCP Storage 경로 변수 설정
GCP_BUCKET="$GCP_BACKUP_BUCKET"

# 디렉토리에서 tar로 묶고 gzip 압축
cd "$BACKUP_DIR" || exit 1
# shellcheck disable=SC2024
if sudo tar -cf - . | sudo gzip > "$BACKUP_FILE"; then
    # GCP Storage 업로드
    if gsutil cp "$BACKUP_FILE" "$GCP_BUCKET"; then
        # 로컬 백업 파일 삭제
        rm -f "$BACKUP_FILE"

        exit 0
    fi
else
    exit 1
fi
