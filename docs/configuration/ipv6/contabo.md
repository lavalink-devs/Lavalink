---
description: How to configure IPv6 on your Contabo VPS.
---

Setting up IPv6 for Lavalink on Contabo VPS is super easy. The VPS comes with a /64 address to use.

This how-to may depend on your system's OS, but it has already been tested on `Ubuntu` and `Debian`.

First, you need to enable IPv6 on your Contabo VPS. It's very easy, as they have already included auto commands in the .bashrc for you.
Enable IPv6 by using this command: `enable_ipv6`

Then reboot your server once, as stated in the Contabo documentation. `reboot`

To see your IPv6 /64 address, simply type: `ip a`

Expected output:
```
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:50:56:51:fc:62 brd ff:ff:ff:ff:ff:ff
    altname enp0s18
    altname ens18
    inet xxx.xxx.xxx.xxx/22 brd xxx.xxx.xxx.xxx scope global eth0
       valid_lft forever preferred_lft forever
    inet6 you-want-to-copy-this-one::1/64 scope global <------- copy this ipv6 /64
       valid_lft forever preferred_lft forever
    inet6 ignore-this-one/64 scope link
       valid_lft forever preferred_lft forever
```

Then we need to enable nonlocal bind: `sysctl -w net.ipv6.ip_nonlocal_bind=1`

And `echo 'net.ipv6.ip_nonlocal_bind=1' >> /etc/sysctl.conf` (So you do not need to adjust sysctl settings again when rebooting the server.)

Then `ip -6 route replace local the-ipv6-you-copied::/64 dev lo` (Don't forget to remove the '1' from the address that you copy from, for example 'you-want-to-copy-this-one::1/64', the '1' after '::'.)

Test your IPv6
```
# Replace 1234:1234:1234:: with your IPv6 address.
ping6 -I 1234:1234:1234:: -c 2 google.com
ping6 -I 1234:1234:1234::1 -c 2 google.com
ping6 -I 1234:1234:1234::2 -c 2 google.com
ping6 -I 1234:1234:1234:dead:beef:1234:1234 -c 2 google.com
```