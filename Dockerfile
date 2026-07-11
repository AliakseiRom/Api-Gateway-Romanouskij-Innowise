FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package

FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]