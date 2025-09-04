# syntax=docker/dockerfile:1
FROM gradle:8.8-jdk17 AS build
WORKDIR /app
# gradle 유저 권한으로 복사 (권장)
COPY --chown=gradle:gradle . .
# wrapper 대신 gradle 바이너리 사용 + 데몬 비활성화
RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
