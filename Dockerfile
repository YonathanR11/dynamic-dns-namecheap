#
# Build stage
#
FROM maven:3.8.2-jdk-11 AS build
COPY . .
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=build /target/dynamic-dns-namecheap-1.0-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Ejecutar desde comandos:
# docker build -t dynamic-dns-namecheap .
# docker run -d --name=dynamic-dns-namecheap dynamic-dns-namecheap -hosts='@,*' -domain_name=example.com -ddns_password=12345678 -timer_schedule=30000
