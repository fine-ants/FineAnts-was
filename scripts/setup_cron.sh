#!/bin/bash

# 설정값
SCRIPT_PATH="/home/ec2-user/build/production/scripts/send_logs_to_s3.sh"

# cron 작업 내용 지정
CRON_JOB="10 0 * * * /bin/bash $SCRIPT_PATH"

# crontab 리스트 리셋
crontab -r

# 현재 crontab에 cron 작업 추가
(crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -

echo "Cron job for '$SCRIPT_PATH' has been added to crontab."

