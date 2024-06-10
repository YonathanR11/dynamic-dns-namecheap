# Usar una imagen base de Java 11
FROM openjdk:11-jre-slim

# Crear un directorio para la aplicación
WORKDIR /app

# Copiar el archivo JAR de la aplicación al contenedor
COPY target/dynamic-dns-namecheap-1.0-SNAPSHOT.jar /app/app.jar

# Comando por defecto para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
