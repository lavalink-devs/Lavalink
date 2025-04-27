---
description: How to configure Lavalink with the Config Server
---

# Config Server

## Example Lavalink application.yml

To configure Lavalink to pull it's configuration from a [Lavalink Config Server](https://github.com/lavalink-devs/Lavalink-Config-Server) you need to put the following in your `application.yml` file:

```yaml title="application.yml"
--8<-- "LavalinkServer/application.yml.cloud-example"
```

Alternatively, this can also be done via environment variables:

<details markdown="1">
<summary>Environment Variables</summary>

```bash
SPRING_APPLICATION_NAME
SPRING_CLOUD_CONFIG_PROFILE
SPRING_CLOUD_CONFIG_LABEL
SPRING_CLOUD_CONFIG_FAIL_FAST

SPRING_CONFIG_IMPORT
```

</details>

## Running the Lavalink Config Server

To run the Lavalink Config Server, you can use the docker image located at [`ghcr.io/lavalink-devs/lavalink-config-server`](https://github.com/lavalink-devs/Lavalink-Config-Server/pkgs/container/lavalink-config-server)

### Example docker-compose.yml

```yaml
services:
  lavalink-config-server:
    image: ghcr.io/lavalink-devs/lavalink-config-server:master
    container_name: lavalink-config-server
    restart: unless-stopped
    environment:
      # set the environment variables for the config server here
    volumes:
      # mount application.yml from the same directory, if you want to use environment variables remove this line below
      - ./application.yml:/opt/Lavalink-Config-Server/application.yml
    networks:
      - lavalink
    expose:
      # lavalink Config Server exposes port 8888 to pull config from
      - 8888
    ports:
      # you only need this if you want to make your Lavalink Config Server accessible from outside of containers, keep in mind this will expose your lavalink Config Server to the internet
      - "8888:8888"
      # if you want to restrict access to localhost only
      # - "127.0.0.1:8888:8888"
networks:
  # create a lavalink network you can add other containers to, to give them access to Lavalink
  lavalink:
    name: lavalink
```

### Example application.yml

The Lavalink Config Server can be configured to use a git repository or a local filesystem as the backend for the configuration files.

```yaml title="application.yml"
spring:
  profiles:
    # Set to native to use a local filesystem/static url
    active: git
  cloud:
    config:
      server:
        # Set to true to allow empty config files to be accepted
        accept-empty: false
        # See: https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_git_backend
        git:
          # The uri supports the following placeholders: {application}, {profile} & {label}
          uri: "https://github.com/lavalink-devs/Lavalink-Example-Configs"
          # set this to {application} if you group your configs by application
          search-paths: "{application}"
          skipSslValidation: false
          timeout: 5
          cloneOnStart: true
          force-pull: false
          deleteUntrackedBranches: false
          refreshRate: 0
          # username: trolley
          # for GitHub, use a personal access token
          # password: strongpassword
          defaultLabel: main
        # See: https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_file_system_backend
        native:
          searchLocations: "file:config/{application"

server:
  port: 8888
  address: 127.0.0.1

logging:
  level:
    root: INFO
    org.springframework.cloud.config: DEBUG
```

Alternatively, this can also be done via environment variables:

<details markdown="1">
<summary>Environment Variables</summary>

```bash
SPRING_PROFILES_ACTIVE

SPRING_CLOUD_CONFIG_SERVER_ACCEPT_EMPTY

SPRING_CLOUD_CONFIG_SERVER_GIT_URI
SPRING_CLOUD_CONFIG_SERVER_GIT_SEARCH_PATHS
SPRING_CLOUD_CONFIG_SERVER_GIT_SKIP_SSL_VALIDATION
SPRING_CLOUD_CONFIG_SERVER_GIT_TIMEOUT
SPRING_CLOUD_CONFIG_SERVER_GIT_CLONE_ON_START
SPRING_CLOUD_CONFIG_SERVER_GIT_FORCE_PULL
SPRING_CLOUD_CONFIG_SERVER_GIT_DELETE_UNTRACKED_BRANCHES
SPRING_CLOUD_CONFIG_SERVER_GIT_REFRESH_RATE
SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME
SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD
SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL

SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS

SERVER_PORT
SERVER_ADDRESS

LOGGING_LEVEL_ROOT
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_CLOUD_CONFIG
```

</details>

### Example config repository

See [Lavalink Example Configs](https://github.com/lavalink-devs/Lavalink-Example-Configs) for an example config repository.

#### Structure

The config repository should have the following structure:

```text
.
├── lavalink
│   ├── application.yml
│   └── application-{profile}.yml
```

The `application.yml` file is the default configuration file.
The `application-{profile}.yml` file is the configuration file for the specified profile and overrides the config entries in the default configuration file.
The profile name can be set in the [`application.yml`](#example-applicationyml) file of the Lavalink server.
