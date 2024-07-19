---
description: How to configure IPv6 on your Hetzner VPS.
---

# US locations for hetzner blockeds ipv6 rotation 

Setting up IPv6 for Lavalink on a Hetzner VPS is just 3 commands. The VPS comes with a /64 address to use and already has IPv6 enabled for you.

This how-to may depend on your system's OS, but it has already been tested on `Ubuntu` and `Debian`.

Look up your IPv6 /64 address on your hetzner cloud panel.

HETZNER Cloud panel -> Your server -> Networking tab -> PUBLIC NETWORK -> PRIMARY IP.

Expected address should be something like this:
`2a01:4f9:xxxx:xxxx::/64`

First, we need to enable nonlocal bind: `sysctl -w net.ipv6.ip_nonlocal_bind=1`

And `echo 'net.ipv6.ip_nonlocal_bind=1' >> /etc/sysctl.conf` (So you do not need to adjust sysctl settings again when rebooting the server.)

Then `ip -6 route replace local 2a01:4f9:xxxx:xxxx::/64 dev lo` (Replace the `2a01:4f9:xxxx:xxxx::/64` with your IPv6 address from panel.)

Test your IPv6
```
# Replace 1234:1234:1234:: with your IPv6 address.
ping6 -I 1234:1234:1234:: -c 2 google.com
ping6 -I 1234:1234:1234::1 -c 2 google.com
ping6 -I 1234:1234:1234::2 -c 2 google.com
ping6 -I 1234:1234:1234:dead:beef:1234:1234 -c 2 google.com
```
