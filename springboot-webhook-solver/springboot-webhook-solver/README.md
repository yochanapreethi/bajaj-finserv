# Webhook Solver (Spring Boot)

## Prerequisites
- Java 17 or newer (`java -version`)
- Maven (`mvn -v`)
- VS Code with:
  - Extension Pack for Java
  - Spring Boot Extension Pack

## Setup
1. Open this folder in VS Code.
2. Edit `src/main/resources/application.properties`.
3. Run:
   ```bash
   mvn spring-boot:run
   ```
   The app runs automatically and prints responses in the terminal.

## Build JAR
```bash
mvn -DskipTests package
java -jar target/webhook-solver-0.0.1-SNAPSHOT.jar
```