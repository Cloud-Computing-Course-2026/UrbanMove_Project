# Multi-stage build para microservicio de reservas (Java 17)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copiar POM padre y POM del módulo
COPY pom.xml /workspace/
COPY services/reservations/pom.xml /workspace/services/reservations/

# Descargar dependencias
RUN mvn -B -f /workspace/services/reservations/pom.xml dependency:go-offline

# Copiar código fuente y compilar
COPY services/reservations/src /workspace/services/reservations/src
RUN mvn -B -f /workspace/services/reservations/pom.xml clean package -DskipTests

# Etapa final de ejecución ligera
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S urbanmove && adduser -S urbanmove -G urbanmove
USER urbanmove:urbanmove

COPY --from=builder /workspace/services/reservations/target/*.jar app.jar

EXPOSE 8106

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
