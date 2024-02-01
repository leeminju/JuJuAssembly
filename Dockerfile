FROM openjdk:17 as build
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/application.properties /application.properties
COPY src/main/resources/application-dev.properties /application-dev.properties
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.config.location=file:/application.properties,file:/application-dev.properties"]
