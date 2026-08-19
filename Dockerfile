
# MARK: build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# MARK: runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN groupadd --gid 10001 greentech \
    && useradd --uid 10001 --gid greentech --no-create-home --home-dir /app greentech \
    && mkdir -p /app/uploads /app/logs \
    && chown -R greentech:greentech /app

COPY --from=builder --chown=greentech:greentech /workspace/build/libs/*.jar app.jar

USER greentech
EXPOSE 41783

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
