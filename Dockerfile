# Dockerfile for fly.io deploy

FROM openjdk:11-slim-buster AS runtime
COPY service.jar /app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
