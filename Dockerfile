FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY src/main/resources/*.tsv /app/src/main/resources/
COPY src/main/resources/*.csv /app/src/main/resources/
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=${PROFILE}","-jar","app.jar"]
