FROM eclipse-temurin:17-alpine
# 작성자 및 메타데이터 추가
LABEL maintainer="fineants.co.2024@gmail.com" \
      version="0.0.16" \
      description="FineAnts Application - Spring Boot App" \
      created="2025-02-19" \
      org.opencontainers.image.source="https://github.com/fine-ants/FineAnts-was" \
      org.opencontainers.image.documentation="https://hub.docker.com/repository/docker/fineants/fineants-app/general" \
      org.opencontainers.image.licenses="MIT"
# 작업 디렉토리 설정
WORKDIR /app
# 빌드된 jar 파일을 복사
COPY ./build/libs/fineAnts_app.jar "app.jar"
# 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]
