If you're using a Systemd-based Linux distribution you may want to install Lavalink as a background service. You will need to create a `lavalink.service` file inside `/usr/lib/systemd/system`. Create the file with the following template (replacing the values inside the `<>` brackets):

```ini
[Unit]
# Describe the service
Description=Lavalink Service

# Configure service order
After=syslog.target network.target

[Service]
# The user which will run Lavalink
User=<usr>

# The group which will run Lavalink
Group=<usr>

# Where the program should start
WorkingDirectory=</home/usr/lavalink>

# The command to start Lavalink
ExecStart=java -Xmx4G -jar </home/usr/lavalink>/Lavalink.jar

# Restart the service if it crashes
Restart=on-failure

# Delay each restart by 5s
RestartSec=5s

[Install]
# Start this service as part of normal system start-up
WantedBy=multi-user.target
```

To initiate the service, run

```shell
sudo systemctl daemon-reload
sudo systemctl enable lavalink
sudo systemctl start lavalink
```

In addition to the usual log files, you can also view the log with `sudo journalctl -u lavalink`.