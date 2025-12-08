#!/bin/bash
# setup_cron.sh

# =========================================================
# 1. 설정: 등록할 Cron 작업 목록 정의
# =========================================================

# GCP VM 내의 배포 경로를 환경 변수에서 가져옵니다.
# 💡 GCP_DEPLOY_PATH는 GitHub Actions에서 이미 설정되었다고 가정합니다.
echo "Deploy base path from GCP_DEPLOY_PATH: $GCP_DEPLOY_PATH"
if [ -z "$GCP_DEPLOY_PATH" ]; then
    echo "Error: GCP_DEPLOY_PATH environment variable is not set."
    exit 1
fi

# 스크립트 경로 정의
BACKUP_MYSQL_SCRIPT_PATH="$GCP_DEPLOY_PATH/scripts/gcp/backup_mysql_data.sh"
echo "Backup MySQL script path: $BACKUP_MYSQL_SCRIPT_PATH"
# ex) LOGS_SCRIPT_PATH="$DEPLOY_BASE_PATH/send_logs_to_gcs.sh" # 필요시 추가

CRON_ENV_FILE="$ENV_FILE"
echo "Crontab environment file path: $CRON_ENV_FILE"

# 💡 등록할 모든 Cron 작업 내용을 배열에 정의합니다.
# 형식: "[분] [시] [일] [월] [요일] [실행 명령어]"
CRON_JOBS=(
    # 1. MySQL 백업 스크립트
    "10 0 * * * ENV_FILE=$CRON_ENV_FILE /bin/bash $BACKUP_MYSQL_SCRIPT_PATH"
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

