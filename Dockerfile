# ---------- Estagio 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S esg && adduser -S esg -G esg
WORKDIR /app
RUN mkdir -p /app/logs && chown -R esg:esg /app
COPY --from=build --chown=esg:esg /build/target/*.jar app.jar

USER esg
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
