#!/bin/bash
# setup_cron.sh

# =========================================================
# 1. 설정: 등록할 Cron 작업 목록 정의
# =========================================================

# GCP VM 내의 배포 경로를 환경 변수에서 가져옵니다.
# 💡 WORK_DIR는 GitHub Actions에서 이미 설정되었다고 가정합니다.
echo "Deploy base path from WORK_DIR: $WORK_DIR"
if [ -z "$WORK_DIR" ]; then
    echo "Error: WORK_DIR environment variable is not set."
    exit 1
fi

# 스크립트 경로 정의
MYSQL_BACKUP_SCRIPT="$WORK_DIR/scripts/gcp/backup_mysql_data.sh"
echo "MySQL Backup script path: $MYSQL_BACKUP_SCRIPT"
LOG_BACKUP_SCRIPT="$WORK_DIR/scripts/gcp/backup_log_data.sh"
# ex) LOGS_SCRIPT_PATH="$DEPLOY_BASE_PATH/send_logs_to_gcs.sh" # 필요시 추가

echo "Crontab environment file path: $ENV_FILE"

# 💡 등록할 모든 Cron 작업 내용을 배열에 정의합니다.
# 형식: "[분] [시] [일] [월] [요일] [실행 명령어]"
CRON_JOBS=(
    "10 0 * * * ENV_FILE=$ENV_FILE /bin/bash $MYSQL_BACKUP_SCRIPT"
    "0 0 * * * ENV_FILE=$ENV_FILE /bin/bash $LOG_BACKUP_SCRIPT"
)

# =========================================================
# 2. Crontab 등록 로직
# =========================================================
# crontab -l 명령의 출력을 저장할 임시 변수
CRONTAB_LIST=""

# crontab 리스트 리셋 (기존 항목 모두 삭제)
echo "Resetting existing crontab..."
crontab -r 2>/dev/null

# 배열의 모든 Cron 작업을 임시 변수에 추가
for JOB in "${CRON_JOBS[@]}"; do
    CRONTAB_LIST+="$JOB\n"
done

# 💡 단일 명령으로 모든 작업을 crontab에 등록
echo -e "$CRONTAB_LIST" | crontab -

echo "=================================================="
echo "✅ Successfully deployed the following cron jobs:"
echo -e "$CRONTAB_LIST"
echo "=================================================="

exit 0

