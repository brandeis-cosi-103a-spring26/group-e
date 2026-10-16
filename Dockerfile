# Build
FROM --platform=linux/amd64 maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY automation/ ./automation/

RUN mvn -f automation/pom.xml clean package -DskipTests

# Create runtime image
FROM --platform=linux/amd64 eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/automation/server/target/server-*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]