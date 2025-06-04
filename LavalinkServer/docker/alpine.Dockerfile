FROM azul/zulu-openjdk-alpine:21-jre-headless-latest

RUN apk add --no-cache libgcc

# Run as non-root user
RUN addgroup -g 322 -S lavalink && \
    adduser -u 322 -S lavalink lavalink

WORKDIR /opt/Lavalink

RUN chown -R lavalink:lavalink /opt/Lavalink

USER lavalink

COPY LavalinkServer/build/libs/Lavalink-musl.jar Lavalink.jar

ENTRYPOINT ["java", "-jar", "Lavalink.jar"]
