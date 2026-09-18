# =========================
# 1. BUILD REACT FRONTEND
# =========================
FROM node:22 AS frontend-build

WORKDIR /frontend

COPY frontend/package*.json ./

RUN npm ci

COPY frontend/ .

RUN npm run build


# =========================
# 2. BUILD SPRING BOOT
# =========================
FROM eclipse-temurin:21-jdk AS backend-build

WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src src

# Put React production files inside Spring Boot
COPY --from=frontend-build /frontend/dist /app/src/main/resources/static/

RUN ./mvnw clean package -DskipTests


# =========================
# 3. RUN FINAL APPLICATION
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 10000

CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]