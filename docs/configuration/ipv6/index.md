---
description: IPv6 related information.
---

# IPv6

Nowadays, most server providers will give you an IPv6 `/64` with a VPS for free. You can easily check by `ping6 google.com` or using `ip a` to check for an IPv6 address.

If your hosting provider stated that they provide IPv6 but your server does not have one, kindly ask them about IPv6 and how to configure it. Sometimes, the server provider might require you to open a ticket for providing IPv6 to your server.

For Lavalink use, most IPv6 rotation plans recommend IPv6 with a block size larger than `/64`, but you can still configure it for use with Lavalink even if it's less than `/64`.

Here are some guides for some popular server providers:
- [Contabo](/docs/configuration/ipv6/contabo.md)
- [DigitalOcean](/docs/configuration/ipv6/digitalocean.md)
- [Hetzner](/docs/configuration/ipv6/hetzner.md)


If your server provider is not listed above, you can use you can check out the general [Debian/Ubuntu](/docs/configuration/ipv6/ubuntudebian.md) guide on how to configure IPv6 on your server.

If your server provider does not provide IPv6 or your IPv6 block size is less than `/64`, you can use [Tunnelbroker](/docs/configuration/ipv6/tunnelbroker.md) instead.

Sometimes, using Tunnelbroker will result in videos that are geo-blocked in every country except the US and Canada, even if the IPv6 address is based in European countries. It may also result in videos that are geo-blocked in the US and Canada, even if the IPv6 address is based in the US.
