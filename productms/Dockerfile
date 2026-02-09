# -------- Build stage --------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline

COPY . .
RUN ./mvnw clean package -DskipTests

# -------- Runtime stage --------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
