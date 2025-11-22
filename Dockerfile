# ============================
# 1. Etapa de build (Maven)
# ============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos solo el pom para cachear dependencias
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR (sin tests para evitar problemas de entorno)
RUN mvn -q -B clean package -DskipTests


# ============================
# 2. Etapa de runtime 
# ============================
FROM eclipse-temurin:17-jre

# Para apps Java, es buena práctica evitar root
RUN useradd -m spring
USER spring

WORKDIR /app

# Copiamos el JAR final
COPY --from=build /app/target/backofficeAPI-1.0-RELEASE.jar app.jar

# Puerto expuesto
EXPOSE 8080

# Opciones para optimizar el rendimiento
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
