spring:
  cloud:
    config:
      # Enable the use of the Config Server
      enabled: true
      # Set the application name here
      name: Lavalink
      # Set profile here (default: default)
      profile: node1
      # Set the label (git branch/commit id/release) (default: main/master)
      label: master
      # Fail if no config could be found
      fail-fast: true
      # Set the username for the Config Server
      username: admin
      # Set the password for the Config Server
      password: "youshallnotpass"
  config:
    # Replace http://localhost:8888 with the url to your Lavalink Config Server
    import: "configserver:http://localhost:8888/"
