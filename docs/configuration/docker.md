---
description: How to run Lavalink as a Docker container
---

# Docker

Docker images can be found under [packages](https://github.com/lavalink-devs/Lavalink/pkgs/container/lavalink) with old builds prior to `v3.7.4` being available on [Docker Hub](https://hub.docker.com/r/fredboat/lavalink/).
There are 2 image variants `Ubuntu` and `Alpine`, the `Alpine` variant is smaller and can be used with the `-alpine` suffix, for example `ghcr.io/lavalink-devs/lavalink:3-alpine`.

Install [Docker](https://docs.docker.com/engine/install/) & [Docker Compose](https://docs.docker.com/compose/install/)

Create a `docker-compose.yml` with the following content:

```yaml
version: "3.8"

services:
  lavalink:
    image: ghcr.io/lavalink-devs/lavalink:4 # pin the image version to Lavalink v4
    container_name: lavalink
    restart: unless-stopped
    environment:
      - _JAVA_OPTIONS=-Xmx6G # set Java options here
      - SERVER_PORT=2333 # set lavalink server port
      - LAVALINK_SERVER_PASSWORD=youshallnotpass # set password for lavalink
    volumes:
      - ./application.yml:/opt/Lavalink/application.yml # mount application.yml from the same directory or use environment variables
      - ./plugins/:/opt/Lavalink/plugins/ # persist plugins between restarts, make sure to set the correct permissions (user: 322, group: 322)
    networks:
      - lavalink
    expose:
      - 2333 # lavalink exposes port 2333 to connect to for other containers (this is for documentation purposes only)
    ports:
      - "2333:2333" # you only need this if you want to make your lavalink accessible from outside of containers
networks:
  lavalink: # create a lavalink network you can add other containers to, to give them access to Lavalink
    name: lavalink
```

Create an `application.yml` file in the same directory as the `docker-compose.yml` file. ([Example here](index.md#example-applicationyml)) or use environment variables ([Example here](index.md#example-environment-variables))

Run `docker compose up -d`. See [Docker Compose Up](https://docs.docker.com/engine/reference/commandline/compose_up/)

If your bot also runs in a docker container you can make that container join the lavalink network and use `lavalink` (service name) as the hostname to connect.
See [Docker Networking](https://docs.docker.com/network/) & [Docker Compose Networking](https://docs.docker.com/compose/networking/)
