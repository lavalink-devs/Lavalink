---
description: How to run Lavalink as a Docker container
---

# Docker

Docker images can be found under [packages](https://github.com/lavalink-devs/Lavalink/pkgs/container/lavalink) with old builds prior to `v3.7.4` being available on [Docker Hub](https://hub.docker.com/r/fredboat/lavalink/).
Install [Docker](https://docs.docker.com/engine/install/) & [Docker Compose](https://docs.docker.com/compose/install/)

## Docker Image Variants

| Variant      | Description                                  | Java Version | User  | Group | Example                                       |
|--------------|----------------------------------------------|--------------|-------|-------|-----------------------------------------------|
| `Ubuntu`     | Default variant, based on Ubuntu             | 18           | 322   | 322   | `ghcr.io/lavalink-devs/lavalink:4`            |
| `Alpine`     | Smaller image size, based on Alpine          | 21           | 322   | 322   | `ghcr.io/lavalink-devs/lavalink:4-alpine`     |
| `Distroless` | Even smaller image size, based on Distroless | 21           | 65534 | 65534 | `ghcr.io/lavalink-devs/lavalink:4-distroless` |

Create a `docker-compose.yml` with the following content:

```yaml title="docker-compose.yml"
services:
  lavalink:
    # pin the image version to Lavalink v4 and use the alpine variant for a smaller image size
    image: ghcr.io/lavalink-devs/lavalink:4-alpine
    container_name: lavalink
    restart: unless-stopped
    environment:
      # set Java options here (6GB heap size)
      - _JAVA_OPTIONS=-Xmx6G
      # set lavalink server port
      # - SERVER_PORT=2333
      # set password for lavalink
      # - LAVALINK_SERVER_PASSWORD=youshallnotpass
    volumes:
      # mount application.yml from the same directory, if you want to use environment variables remove this line below
      - ./application.yml:/opt/Lavalink/application.yml
      # persist plugins between restarts, make sure to create the folder & set the correct permissions and user/group id mentioned above
      - ./plugins/:/opt/Lavalink/plugins/
    networks:
      - lavalink
    expose:
      # lavalink exposes port 2333 to connect to for other containers (this is for documentation purposes only)
      - 2333
    ports:
      # you only need this if you want to make your lavalink accessible from outside of containers, keep in mind this will expose your lavalink to the internet
      - "2333:2333"
      # if you want to restrict access to localhost only
      # - "127.0.0.1:2333:2333"
networks:
  # create a lavalink network you can add other containers to, to give them access to Lavalink
  lavalink:
    name: lavalink
```

Create an `application.yml` file in the same directory as the `docker-compose.yml` file. ([Example here](index.md#example-applicationyml)) or use environment variables ([Example here](index.md#example-environment-variables))

Run `docker compose up -d`. See [Docker Compose Up](https://docs.docker.com/engine/reference/commandline/compose_up/)

If your bot also runs in a docker container you can make that container join the lavalink network and use `lavalink` (service name) as the hostname to connect.
See [Docker Networking](https://docs.docker.com/network/) & [Docker Compose Networking](https://docs.docker.com/compose/networking/)
