FROM azul/zulu-openjdk-alpine:17-jre-headless-latest

RUN apk add --no-cache libgcc

# Run as non-root user
RUN addgroup -g 322 -S lavalink && \
    adduser -u 322 -S lavalink lavalink

WORKDIR /opt/Lavalink

RUN chown -R lavalink:lavalink /opt/Lavalink

USER lavalink

COPY LavalinkServer/build/libs/Lavalink-musl.jar Lavalink.jar

ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.1,TLSv1.2,TLSv1.3", "-jar", "Lavalink.jar"]
