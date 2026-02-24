FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# Cấp quyền execute cho mvnw
RUN chmod +x mvnw

# Build project
RUN ./mvnw clean package -DskipTests

CMD ["java", "-jar", "target/route-app-api-0.0.1-SNAPSHOT.jar"]