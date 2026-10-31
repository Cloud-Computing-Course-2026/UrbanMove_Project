FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml checkstyle.xml ./
COPY src ./src
RUN mvn -B -ntp verify

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S rides && adduser -S rides -G rides
WORKDIR /app
COPY --from=build /build/target/rides-0.1.0-SNAPSHOT.jar app.jar
USER rides
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
