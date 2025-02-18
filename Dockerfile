FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY src/main/resources/*.tsv /app/src/main/resources/
ARG JAR_FILE="app.jar"
COPY ./build/libs/fineAnts_app.jar /app/${JAR_FILE}
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=${PROFILE} -jar /app/${JAR_FILE}"]
