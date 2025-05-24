# Stage 1: Build app using Maven + JDK 22
FROM maven:3.9-eclipse-temurin-22 as builder

WORKDIR /app
COPY . .
COPY .env .env
RUN mvn clean package -DskipTests

# Stage 2: Run app with lighter JDK 22 image
FROM eclipse-temurin:22-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/.env .env

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
