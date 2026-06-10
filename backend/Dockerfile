# Step 1: Build the application using Eclipse Temurin Java 21

FROM maven:3.9.6-eclipse-temurin-21 AS build

COPY . .

RUN mvn clean package -DskipTests
 
# Step 2: Run the application using a safe Java 21 JRE runtime

FROM eclipse-temurin:21-jre-alpine

COPY --from=build /target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
 