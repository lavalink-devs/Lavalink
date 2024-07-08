---
description: How to configure IPv6 on your DigitalOcean VPS.
---

As the IPv6 rotation plan suggests, it is recommended to use a block size larger than /64. However, with DigitalOcean, they only provides you with 16 IPv6 addresses. If you prefer to use an IPv6 /64 or /48 block size for your DigitalOcean VPS, consider this alternative instead. [Using Tunnelbroker to make Lavalink balance its requests over many IPv6 addresses](https://blog.arbjerg.dev/2020/3/tunnelbroker-with-lavalink)

As of now, if you want to use Tunnelbroker with DigitalOcean, you might encounter a block when trying to create a tunnel for your DigitalOcean IP. You may need to contact them to unblock your server.

This how-to may depend on your system's OS, but it has already been tested on `Ubuntu` and `Debian`.

### Create DigitalOcean Droplet with IPv6 enabled.

In `Create Droplets` panel -> `Advanced Options` -> Click `Enable IPv6 (free)`

### Enable IPv6 on existed DigitalOcean Droplet.

Go to your Droplet panel -> Turn off your Droplet -> `Networking` -> `PUBLIC IPV6 ADDRESS` -> `Enable` -> Then turn your Droplet on again.

After you enable IPv6 on the existing Droplet, you need to configure it manually. Please refer to your OS System in this link. [https://docs.digitalocean.com/products/networking/ipv6/how-to/enable/](https://docs.digitalocean.com/products/networking/ipv6/how-to/enable/)

After that, reboot your server once: `reboot`

Test your IPv6 connection: `ping6 google.com` or `ping6 2001:4860:4860::8888`

### [Optional] use all 16 IPv6 Addresses

Please refer to your OS System in this link. [How to Enable Additional IPv6 Addresses](https://docs.digitalocean.com/products/networking/ipv6/how-to/configure-additional-addresses/)

Test your IPv6
```
# Don't forgot to replace IPv6 in the example with your IPv6 address.
ping6 -I 2400:6180:0:d0::fa6:2000 -c 2 google.com
ping6 -I 2400:6180:0:d0::fa6:2001 -c 2 google.com
ping6 -I 2400:6180:0:d0::fa6:2002 -c 2 google.com
ping6 -I 2400:6180:0:d0::fa6:2003 -c 2 google.com
ping6 -I 2400:6180:0:d0::fa6:2004 -c 2 google.com
...
ping6 -I 2400:6180:0:d0::fa6:200f -c 2 google.com
```