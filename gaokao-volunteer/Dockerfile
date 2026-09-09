FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build
COPY backend/pom.xml .
RUN apt-get update && apt-get install -y maven && mvn -q -DskipTests dependency:go-offline
COPY backend/ .
RUN mvn -q -DskipTests clean package

FROM node:18-alpine AS frontend
WORKDIR /build
COPY frontend/package.json .
RUN npm install --silent
COPY frontend/ .
RUN npm run build

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
COPY --from=frontend /build/dist ./static
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]