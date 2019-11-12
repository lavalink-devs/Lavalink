# RoutePlanner Strategies

***Terminology***

**Ip block / CIDR block**

An IP block or CIDR block are addresses which are next to each other

(e.g.) 1.0.0.0, 1.0.0.1, 1.0.0.2, ...

For more information about CIDR blocks, refer to [this article](https://docs.netgate.com/pfsense/en/latest/book/network/understanding-cidr-subnet-mask-notation.html).

**Combined IP Block**

A combined IP Block is created when multiple IPs / CIDR blocks are added to the 
configuration file. Lavaplayer will treat them as a single virtual IP-Block.

## RotateOnBan Strategy
### RoutePlanner
This strategy uses the `RotatingIpRoutePlanner`.
### Implementation
This strategy switches the ip as soon as the used address got banned.
### Requirements / Recommendations
The usage of this strategy is recommended for IPv4 blocks or smaller IPv6 blocks 
(less than a /64)

## LoadBalance Strategy
### RoutePlanner
This strategy uses the `BalancingIpRoutePlanner`.
### Implementation
This strategy chooses random addresses from the given ip-block.
### Requirements / Recommendations
YouTube already bans ips from their service when they just do a few automated
requests per day. Due to this you should have a bigger IPv6 CIDR to use this 
strategy.

This strategy does not require a big CIDR to work, but you will most likely 
get banned when you have not enough IP's in the used CIDR.

## NanoSwitch Strategy
### RoutePlanner
This strategy uses the `NanoIpRoutePlanner`.
### Implementation
This strategy switches the IP on each clock update and uses the current nanosecond
as offset in the used block.
### Requirements / Recommendations
This strategy requires at least a (combined) /64 IPv6 CIDR (18 quintillion 
addresses).

If you have a CIDR which is (combined) bigger than a /64, it is recommended to use
`RotatingNanoSwitch` instead.

## RotatingNanoSwitch Strategy
### RoutePlanner
This strategy uses the `RotatingNanoIpRoutePlanner`.
### Implementation
This strategy switches the ip on each clock-update and uses the current nano-time
as offset in the used block.

When a ban occurs, this strategy rotates to the next /64 block as fallback strategy.
### Requirements / Recommendations
This strategy requires at least a /64 IPv6 CIDR (18 quintillion 
addresses).

For a working rotation you need at least 2 /64 IPv6 CIDRs.

It is not recommended using combined /64s here (e.g. 2 different /65s), though you can 
combine multiple /64s (or bigger) without issues, because this strategy 
rotates on these.
