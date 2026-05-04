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

# Copy entity sources from project root (pom.xml references ../src/main/java)
# Maven resolves ${project.basedir}/../src = /app/../src = /src
COPY src/ /src/

# Build the fat JAR
RUN mvn clean package -DskipTests -B \
    -Dhttps.protocols=TLSv1.2 \
    --no-transfer-progress

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
