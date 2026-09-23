# Runtime image for the single-jar build (API + bundled UI).
#   bazel build --config=ui //backend:server_with_ui_deploy.jar
#   cp -f bazel-bin/backend/server_with_ui_deploy.jar deploy/app.jar
#   docker build -t adsphere deploy/
# (bazel-bin is a symlink outside the build context, hence the copy.)
FROM eclipse-temurin:21-jre

RUN useradd --system --uid 10001 app
WORKDIR /app
COPY app.jar app.jar
USER app

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED -Dnet.bytebuddy.experimental=true"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
