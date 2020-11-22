# RoutePlanner Strategies

## Terminology

**Ip block / CIDR block**

An IP block is a collection of IP addresses, usually contiguous and usually described in the form of CIDR notation.

For more information about CIDR blocks, refer to [this article](https://docs.netgate.com/pfsense/en/latest/book/network/understanding-cidr-subnet-mask-notation.html).

**Combined IP Block**

A combined IP Block is created when multiple CIDR blocks are added to the 
configuration file. Lavaplayer will treat them as a single virtual IP Block.

## RotateOnBan Strategy
This strategy uses the `RotatingIpRoutePlanner`. It switches the ip as soon as the used address got banned.
It is recommended for IPv4 blocks or IPv6 blocks with a bitmask lower than 64.

## LoadBalance Strategy
This strategy is the `BalancingIpRoutePlanner`.
It selects random addresses from the given IP block. Some services ban IPs from when you do just a few automated
requests per day. Due to this you should have a bigger block to effectively use this strategy.

## NanoSwitch Strategy
This strategy uses the `NanoIpRoutePlanner`. It switches the IP on each clock update and uses the current nanosecond
in the offset in the used block. This strategy requires at least a (combined) /64 IPv6 block (2⁶⁴ addresses).

If you have a CIDR which is (combined) bigger than a /64, it is recommended to use `RotatingNanoSwitch` instead.

## RotatingNanoSwitch Strategy
This strategy uses the `RotatingNanoIpRoutePlanner`. It switches the IP on each clock update and uses the current nanosecond
in the offset of the used block.

When a ban occurs, this strategy rotates to the next /64 block as fallback strategy.
This strategy requires at least a /64 IPv6 CIDR (2⁶⁴ addresses).

For a working rotation you need at least 2 /64 IPv6 CIDRs.

It is not recommended using combined /64s here (e.g. 2 different /65s), though you can 
combine multiple /64s (or bigger) without issues, because this strategy 
rotates on these.
