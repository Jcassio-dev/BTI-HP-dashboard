
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY certs/rnp-icpedu-gr46-ov-tls-ca-2025.pem /tmp/rnp.pem
RUN keytool -importcert -noprompt -alias rnp-icpedu-gr46-2025 \
        -file /tmp/rnp.pem -cacerts -storepass changeit \
    && rm /tmp/rnp.pem

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]