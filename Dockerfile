# ---- Runtime only (JAR은 CodeDeploy가 /opt/siso에 내려줌) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# CodeDeploy가 내려준 JAR만 복사
COPY app.jar /app/app.jar

# 선택: JVM 옵션/타임존(원하면 사용)
ENV JAVA_OPTS=""
# ENV TZ=Asia/Seoul

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
