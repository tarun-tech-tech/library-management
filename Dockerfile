FROM eclipse-temurin:21.0.8_9-jdk-jammy AS build

WORKDIR /workspace
RUN apt-get update \
    && apt-get install --no-install-recommends --yes ca-certificates curl unzip \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml mvnw ./
COPY .mvn ./.mvn
RUN chmod +x mvnw
RUN ./mvnw -B -ntp -DskipTests dependency:go-offline
COPY src ./src
RUN ./mvnw -B -ntp -DskipTests package

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
