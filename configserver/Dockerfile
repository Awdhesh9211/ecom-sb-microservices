# -------- Build stage --------
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Copy wrapper first (better caching)
COPY .mvn .mvn
COPY mvnw mvnw
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies first (faster builds later)
RUN ./mvnw -q dependency:go-offline

# Now copy full source and build JAR (no tests)
COPY . .
RUN ./mvnw clean package -DskipTests

# -------- Runtime stage (lighter image) --------
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Pick the built jar
COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
