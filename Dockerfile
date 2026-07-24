# Build stage
FROM gradle:8.4-jdk21 as builder

WORKDIR /build

COPY . .

RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /opt/Lavalink

# Create non-root user
RUN groupadd -g 322 lavalink && \
    useradd -r -u 322 -g lavalink lavalink && \
    chown -R lavalink:lavalink /opt/Lavalink

USER lavalink

# Copy JAR from builder
COPY --from=builder /build/LavalinkServer/build/libs/Lavalink.jar Lavalink.jar

# Expose port (Railway akan assign port via $PORT variable)
EXPOSE 2333

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:2333/info || exit 1

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "Lavalink.jar"]
