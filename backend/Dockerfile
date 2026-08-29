
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# curl-impersonate: cliente com assinatura TLS de navegador, para o login no SIGAA passar
# pelo filtro anti-robo da UFRN. Binario autocontido (BoringSSL), so precisa de glibc.
ADD https://github.com/lexiforest/curl-impersonate/releases/download/v2.1.1/curl-impersonate-v2.1.1.x86_64-linux-gnu.tar.gz /tmp/ci.tar.gz
RUN tar -xzf /tmp/ci.tar.gz -C /usr/local/bin \
    && chmod +x /usr/local/bin/curl-impersonate /usr/local/bin/curl_* \
    && rm /tmp/ci.tar.gz

COPY certs/rnp-icpedu-gr46-ov-tls-ca-2025.pem /tmp/rnp.pem
RUN keytool -importcert -noprompt -alias rnp-icpedu-gr46-2025 \
        -file /tmp/rnp.pem -cacerts -storepass changeit \
    && rm /tmp/rnp.pem

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]