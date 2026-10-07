FROM gcr.io/distroless/java25-debian13:nonroot

WORKDIR /opt/Lavalink

COPY LavalinkServer/build/libs/Lavalink.jar Lavalink.jar

ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "--enable-native-access=ALL-UNNAMED", "-jar"]

CMD ["Lavalink.jar"]
