# Use official OpenJDK image
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy project files
COPY . .

# Build project
RUN ./mvnw clean package -DskipTests

# Run application
CMD ["java", "-jar", "target/route-app-api-0.0.1-SNAPSHOT.jar"]