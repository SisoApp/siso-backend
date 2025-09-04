# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM gradle:8.8-jdk17 AS build
# 프로젝트 위치를 siso-backend로 명확히 지정
WORKDIR /app/siso-backend

# 소스 복사 (루트 기준으로 siso-backend만 복사)
COPY --chown=gradle:gradle siso-backend/ ./

# Gradle 데몬 끄고 빌드
RUN gradle clean bootJar -x test --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# 빌드 결과 JAR 복사
COPY --from=build /app/siso-backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
