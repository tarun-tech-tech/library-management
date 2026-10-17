FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./
COPY src ./src

# Render's shared build IP can receive temporary 429 responses from Maven Central.
# Use one dependency-resolution pass, keep resolver concurrency low, and retry with
# backoff instead of downloading Maven through the project wrapper first.
RUN set -eux; \
    for attempt in 1 2 3 4 5; do \
      mvn -B -ntp \
        -Daether.connector.basic.threads=1 \
        -Dmaven.wagon.http.retryHandler.count=5 \
        -DskipTests package && exit 0; \
      if [ "$attempt" -lt 5 ]; then sleep $((attempt * 30)); fi; \
    done; \
    exit 1

FROM eclipse-temurin:21.0.8_9-jre-jammy

WORKDIR /app
RUN apt-get update \
    && apt-get install --no-install-recommends --yes curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 10001 smartlib
COPY --from=build /workspace/target/smartlib-backend-0.1.0-SNAPSHOT.jar app.jar
USER smartlib
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=12 CMD curl --fail --silent http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
