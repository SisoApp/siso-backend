# ---- Runtime only (JAR은 CodeDeploy가 /opt/siso 에 내려줌) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# 번들에 포함된 실행 JAR (deploy/build/libs/.. → /opt/siso/build/libs/..)
COPY build/libs/*.jar /app/app.jar

ENV JAVA_OPTS=""
EXPOSE 8080
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
