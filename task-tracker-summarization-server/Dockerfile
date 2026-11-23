FROM gradle:jdk25 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

COPY src ./src

RUN gradle bootJar --no-daemon


FROM eclipse-temurin:25-jre

WORKDIR /app

COPY certs/ /tmp/certs/
RUN set -e; for f in /tmp/certs/*; do \
      keytool -importcert -trustcacerts -cacerts -storepass changeit -noprompt \
        -alias "$(basename "$f")" -file "$f"; \
    done && rm -rf /tmp/certs

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]