# syntax=docker/dockerfile:1
FROM gradle:8.8-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
