FROM openjdk:9-jre-slim

WORKDIR /opt/Lavalink

COPY LavalinkServer/build/libs/Lavalink.jar Lavalink.jar

ENTRYPOINT ["java", "-jar", "-Xmx4G", "Lavalink.jar"]
