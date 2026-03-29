FROM eclipse-temurin:25-jre-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ARG SPRING_PROFILE=prod
ENV SPRING_PROFILE=${SPRING_PROFILE}

ENTRYPOINT ["java", "-jar", "/app.jar"]