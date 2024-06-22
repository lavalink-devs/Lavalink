---
description: How to configure IPv6 using Tunnelbroker on your VPS.
---

This how-to is for super-newbie and may depend on your system's OS, but it has already been tested on `Ubuntu (22, 23 and 24)` with `netplan`.

### Hello Tunnelbroker!

1. Create an account at [Tunnelbroker](https://tunnelbroker.net)
2. Create a new `Create Regular Tunnel`
3. Enter your server IPv4 and select the Tunnelbroker server with the lowest ping to your server. `ping xxx.xxx.xxx.xxx`
4. (Optional) Get IPv6 /48 block by clicking `assign /48`
5. Go to `Example Configurations`
6. Select `Linux (netplan 0.103+)`
7. Copy the whole config

[WARNING] `xxxx` or `xxx` for example: `2001:470:xxxx:xxx::2/64` is just for censoring real information PLEASE REPLACE IT WITH YOUR IPV6

Example configuration generated from tunnelbroker.net
```yml
network:
  version: 2
  tunnels:
    he-ipv6:
      mode: sit
      remote: 216.218.142.50
      local: 195.xxx.xx.xxx
      addresses:
        - "2001:470:xxxx:xxx::2/64"
      routes:
        - to: default
          via: "2001:470:xxxx:xxx::1"
```

On your server, go to the netplan folder: `cd /etc/netplan`

Create a new file using your choice of text editor, but in this how-to, we will be using nano.
`nano 99-he-tunnel.yaml`

Then paste the whole configuration into that file.

Example configuration of 99-he-tunnel.yaml with /64
```
network:
  version: 2
  tunnels:
    he-ipv6:
      mode: sit
      remote: 216.218.142.50
      local: 195.xxx.xx.xxx
      addresses:
        - "2001:470:xxxx:xxx::2/64"
      routes:
        - to: default
          via: "2001:470:xxxx:xxx::1"
```

Example configuration of 99-he-tunnel.yaml with /48
```
network:
  version: 2
  tunnels:
    he-ipv6:
      mode: sit
      remote: 216.218.142.50
      local: 195.xxx.xx.xxx
      addresses:
        - "2001:470:xxxx::2/48"
      routes:
        - to: default
          via: "2001:470:xxxx::1"
```

### [Optional] You already had IPv6 but still want to use Tunnelbroker.
Most hosting providers will have an OS image that is generated configuration called `50-cloud-init.yaml`.

Start edit that file: `nano 50-cloud-init.yaml`

Example configuration from 50-cloud-init.yaml
```
network:
    ethernets:
        ens3:
            addresses:
            - 195.xxx.xx.xxx/23
            - 2402:xxxx:xxxx::xx/128
            - 2402:xxxx:xxxx:xxxx::a/56
            gateway4: 195.xxx.xx.x
            gateway6: 2402:xxxx:xxxx::1
            match:
                macaddress: 00:34:a0:e1:de:5d
            nameservers:
                addresses:
                - 8.8.8.8
                - 8.8.4.4
                - 2001:4860:4860::8888
                - 2001:4860:4860::8844
            routes:
            -   scope: link
                to: 195.xxx.xx.x
                via: 0.0.0.0
            -   scope: link
                to: 2402:xxxx:xxxx::1
                via: ::0
    version: 2
```

We will be removing
```
            addresses:
            - 195.xxx.xx.xxx/23
            - 2402:xxxx:xxxx::xx/128 <---
            - 2402:xxxx:xxxx:xxxx::a/56 <---
            gateway4: 195.xxx.xx.x
            gateway6: 2402:xxxx:xxxx::1 <---
```

And
```
            -   scope: link <---
                to: 2402:xxxx:xxxx::1 <---
                via: ::0 <---
```

Final configuration
```
network:
    ethernets:
        ens3:
            addresses:
            - 195.xxx.xx.xxx/23
            gateway4: 195.xxx.xx.x
            match:
                macaddress: 00:34:a0:e1:de:5d
            nameservers:
                addresses:
                - 8.8.8.8
                - 8.8.4.4
                - 2001:4860:4860::8888
                - 2001:4860:4860::8844
            routes:
            -   scope: link
                to: 195.xxx.xx.x
                via: 0.0.0.0
    version: 2
```

### Applying netplan and checking your IPv6

Let try our new configuration: `netplan try`

Ignore the warning and if there is no error then just press enter to accept new configuration.

Reboot server one time: `reboot`

Try your IPv6 from Tunnelbroker: `ping6 google.com`
Try curl to make sure that it is Tunnelbroker IPv6: `curl -6 https://ifconfig.co`
And it should return `2001:470:xxxx:xxx::2` or `2001:470:xxxx::2` if you are using /48

Next, we're going to configure IPv6 for Lavalink.
Enable non local binding
`sysctl -w net.ipv6.ip_nonlocal_bind=1`
And so the next time you don't need to type it again
`echo 'net.ipv6.ip_nonlocal_bind = 1' >> /etc/sysctl.conf`

Now, we replace this command with our IPv6 from Tunnelbroker.
`ip -6 route replace local 2001:470:xxxx:xxx::/64 dev lo`
or if you are using /48
`ip -6 route replace local 2001:470:xxxx::/48 dev lo`

Now test your new configuration
```cmd
ping6 -I 2001:470:xxxx:xxx:dead::beef google.com
```
or if you are using /48
```cmd
ping6 -I 2001:470:xxxx:dead::beef google.com
```

Now you can use your new IPv6 `2001:470:xxxx:xxx::/64` or `2001:470:xxxx::/48` to put in your Lavalink configuration.