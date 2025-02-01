FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /opt/Lavalink

COPY LavalinkServer/build/libs/Lavalink.jar Lavalink.jar

ENTRYPOINT ["java", "-jar"]

CMD ["Lavalink.jar"]
