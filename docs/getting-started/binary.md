---
description: How to run Lavalink as a standalone binary
---

# Standalone Binary

## Prerequisites

Install Java 17 or higher. You can download it [here](https://www.azul.com/downloads/?package=jdk#zulu).

## Installation

Download the latest `Lavalink.jar` from [GitHub](https://github.com/lavalink-devs/Lavalink/releases/latest).

Create a new directory and place the `Lavalink.jar` file inside it. This will be your Lavalink installation directory.

## Configuration

Check out the [configuration](../configuration/index.md) page to learn how to configure Lavalink.
The recommended way would be to use a [Config File](../configuration/config-file.md).

## Running Lavalink

Now run the following command in the directory where you placed the `Lavalink.jar` file.

```bash
java -jar Lavalink.jar
```

Now keep your terminal open and wait for Lavalink to start.
If you want to run Lavalink in the background we recommend checking out either [Screen](https://www.gnu.org/software/screen/), [Systemd](systemd.md), or [Docker](docker.md).
