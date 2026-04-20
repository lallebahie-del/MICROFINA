FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . .
# S'assurer que le script mvnw est exécutable
RUN chmod +x mvnw
# Installation des dépendances
RUN ./mvnw clean install -DskipTests
# Commande pour lancer l'application avec les arguments de sécurité pour TLS
ENTRYPOINT ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-Djdk.tls.client.protocols=TLSv1,TLSv1.1,TLSv1.2 -Djava.security.properties=java.security'"]
