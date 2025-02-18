FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY src/main/resources/*.tsv /app/src/main/resources/
COPY ./build/libs/fineAnts_app.jar "app.jar"
ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]
