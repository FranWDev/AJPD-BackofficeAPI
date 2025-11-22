# =======================================
# DOCKERFILE CON FALLBACK - PRODUCCIÓN (Render-friendly)
# =======================================

# =======================================
# 1. BUILD STAGE
# =======================================
FROM eclipse-temurin:17-jdk-alpine-3.22 AS build

RUN apk add --no-cache maven curl bash

ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Dmaven.artifact.threads=8"
ENV MAVEN_CONFIG=/root/.m2

WORKDIR /app

COPY pom.xml ./
COPY mvnw* ./
COPY .mvn .mvn

RUN chmod +x mvnw 2>/dev/null || echo "No mvnw found, will use system maven"

RUN if [ -f mvnw ]; then \
      ./mvnw dependency:go-offline -B; \
    else \
      mvn dependency:go-offline -B; \
    fi

COPY src ./src

RUN if [ -f mvnw ]; then \
      ./mvnw clean package -Pproduction -T 1C; \
    else \
      mvn clean package -Pproduction -T 1C; \
    fi && \
    ls -lh target/*.jar

# =======================================
# 2. EXTRACT STAGE
# =======================================
FROM eclipse-temurin:17-jre-alpine-3.22 AS extract

WORKDIR /extract

COPY --from=build /app/target/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract --destination .

# =======================================
# 3. RUNTIME STAGE
# =======================================
FROM eclipse-temurin:17-jre-alpine-3.22

LABEL maintainer="org.dubini" \
      application="backofficeAPI" \
      version="1.0-RELEASE"

RUN apk add --no-cache dumb-init curl tzdata && \
    rm -rf /var/cache/apk/*

ENV TZ=Europe/Madrid
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

RUN addgroup -S spring && \
    adduser -S spring -G spring -h /app && \
    chown -R spring:spring /app

USER spring:spring

COPY --from=extract --chown=spring:spring /extract/dependencies/ ./
COPY --from=extract --chown=spring:spring /extract/spring-boot-loader/ ./
COPY --from=extract --chown=spring:spring /extract/snapshot-dependencies/ ./
COPY --from=extract --chown=spring:spring /extract/application/ ./

ENV JAVA_TOOL_OPTIONS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:MinRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=100 \
    -XX:+UseStringDeduplication \
    -XX:+ParallelRefProcEnabled \
    -XX:+DisableExplicitGC \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Europe/Madrid"

ENV SPRING_PROFILES_ACTIVE=production

# No fijamos puerto, Spring lo leerá de la variable PORT

HEALTHCHECK --interval=30s \
            --timeout=5s \
            --start-period=60s \
            --retries=3 \
    CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "org.springframework.boot.loader.launch.JarLauncher"]
