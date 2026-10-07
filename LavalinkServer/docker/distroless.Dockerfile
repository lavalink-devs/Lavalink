FROM gcr.io/distroless/java25-debian13:nonroot

WORKDIR /opt/Lavalink

COPY LavalinkServer/build/libs/Lavalink.jar Lavalink.jar

ENTRYPOINT ["java", "-jar"]

CMD ["Lavalink.jar"]
