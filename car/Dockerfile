FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/car-0.0.1-SNAPSHOT.jar.original
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
