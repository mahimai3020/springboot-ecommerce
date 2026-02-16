FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/app.jar app.jar

ENTRYPOINT ["java","-Xms128m","-Xmx256m","-XX:+UseSerialGC","-jar","app.jar"]

EXPOSE 8080
