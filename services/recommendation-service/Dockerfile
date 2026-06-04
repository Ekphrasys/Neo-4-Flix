FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre

# Création d'un user non-root pour éxécuter l'appli en toute sécurité
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

WORKDIR /app

COPY --from=build --chown=appuser:appgroup /app/target/*.jar ./app.jar
USER appuser
EXPOSE 8084
ENTRYPOINT ["java","-jar","/app/app.jar"]
