---
description: How to configure IPv6 on your Ubuntu/Debian VPS.
---

Setting up IPv6 for Lavalink on a Ubuntu/Debian VPS can be challenging depending on how your provider sets up the OS system for you, but most of the time, it is pretty easy.

Mostly, the provider will already set up IPv6 for you. You can find the IPv6 address by using the `ip a` command.

Expected output:
```
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 00:50:56:51:fc:62 brd ff:ff:ff:ff:ff:ff
    altname enp0s18
    altname ens18
    inet xxx.xxx.xxx.xxx/22 brd xxx.xxx.xxx.xxx scope global eth0
       valid_lft forever preferred_lft forever
    inet6 you-want-to-copy-this-one::1/64 scope global <------- this is your ipv6 /64
       valid_lft forever preferred_lft forever
    inet6 ignore-this-one/64 scope link
       valid_lft forever preferred_lft forever
```

If not, kindly ask your provider about providing an IPv6 address for your server and how to configure it.

First, we need to enable nonlocal bind: `sysctl -w net.ipv6.ip_nonlocal_bind=1`

And `echo 'net.ipv6.ip_nonlocal_bind=1' >> /etc/sysctl.conf` (So you do not need to adjust sysctl settings again when rebooting the server.)

Then `ip -6 route replace local 2a01:4f9:xxxx:xxxx::/64 dev lo` (Replace the `2a01:4f9:xxxx:xxxx::/64` with your IPv6 address from `ip a` command.) (Don't forget to remove the '1' from the address that you copy from, for example 'you-want-to-copy-this-one::1/64', the '1' after '::'.)

Test your IPv6
```
# Replace 1234:1234:1234:: with your IPv6 address.
ping6 -I 1234:1234:1234:: -c 2 google.com
ping6 -I 1234:1234:1234::1 -c 2 google.com
ping6 -I 1234:1234:1234::2 -c 2 google.com
ping6 -I 1234:1234:1234:dead:beef:1234:1234 -c 2 google.com
```