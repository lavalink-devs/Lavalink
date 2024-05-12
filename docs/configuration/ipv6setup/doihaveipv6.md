---
description: Check your server IPv6.
---

Nowadays, most server providers will give you an IPv6 /64 with a VPS for free. You can easily check by `ping6 google.com` or using `ip a` to check for an IPv6 address.

If your hosting provider stated that they provide IPv6 but your server does not have one, kindly ask them about IPv6 and how to configure it. Sometimes, the server provider might require you to open a ticket for providing IPv6 to your server.

For Lavalink use, most IPv6 rotation plans recommend IPv6 with a block size larger than /64, but you can still configure it for use with Lavalink even if it's less than /64.

If you wish to use IPv6 but your server providers do not provided one or your IPv6 block size is less than /64 please prefer to [Using Tunnelbroker to make Lavalink balance its requests over many IPv6 addresses](https://blog.arbjerg.dev/2020/3/tunnelbroker-with-lavalink).