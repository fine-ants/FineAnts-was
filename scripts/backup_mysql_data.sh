#!/bin/bash
# backup_mysql_data.sh

# 백업 대상 디렉토리
BACKUP_DIR="/home/fineants/FineAnts-was/mysql/data"

# 백업 파일 경로
BACKUP_FILE="/home/fineants/mysql-backup.gz"

# 메일 설정
EMAIL="fineants.co.2024@gmail.com"
SUBJECT="MySQL Backup Status"

# 디렉토리에서 tar로 묶고 gzip 압축
cd "$BACKUP_DIR" || exit 1
if sudo tar -cf - . | sudo gzip > "$BACKUP_FILE"; then
    # GCP Storage 업로드
    if gsutil cp "$BACKUP_FILE" gs://gcp-fineants-bucket/production/mysql/; then
        # 로컬 백업 파일 삭제
        rm -f "$BACKUP_FILE"

        # 성공 메일 전송
        echo "MySQL data backup completed and uploaded to GCP Storage successfully." | mail -s "$SUBJECT" "$EMAIL"
        echo "MySQL data backup completed and uploaded to GCP Storage."
        exit 0
    else
        echo "Failed to upload backup to GCP Storage." | mail -s "$SUBJECT" "$EMAIL"
        exit 2
    fi
else
    echo "Failed to create backup file." | mail -s "$SUBJECT" "$EMAIL"
    exit 1
fi
