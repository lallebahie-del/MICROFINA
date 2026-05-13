# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Build context is the MICROFINA root directory (set in docker-compose.yaml)
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy POM + Maven wrapper first (cache dependency layer)
COPY backend/pom.xml .
COPY backend/.mvn/ .mvn/
COPY backend/mvnw .
RUN chmod +x mvnw

# Download all dependencies — cached unless pom.xml changes
RUN mvn dependency:go-offline -B \
    -Dhttps.protocols=TLSv1.2 \
    --no-transfer-progress \
    || true

# Copy backend source code
COPY backend/src/ src/

# NOTE: The domain packages (com.microfina.*) now live inside backend/src,
# so we no longer copy a top-level /src directory into the image.

# Build the fat JAR
RUN mvn clean package -DskipTests -B \
    -Dhttps.protocols=TLSv1.2 \
    --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

# curl est utilisé par le healthcheck défini dans docker-compose.yaml
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
