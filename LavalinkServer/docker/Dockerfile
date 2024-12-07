FROM eclipse-temurin:18-jre-jammy

# Run as non-root user
RUN groupadd -g 322 lavalink && \
    useradd -r -u 322 -g lavalink lavalink

WORKDIR /opt/Lavalink

RUN chown -R lavalink:lavalink /opt/Lavalink

USER lavalink

COPY LavalinkServer/build/libs/Lavalink.jar Lavalink.jar

ENTRYPOINT ["java", "-jar", "Lavalink.jar"]
