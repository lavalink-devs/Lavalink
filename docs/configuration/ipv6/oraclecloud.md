---
description: How to configure IPv6 on your Oracle Cloud VPS.
---

## Setting up IPv6 on Oracle Cloud using TunnelBroker Tunnel (48 oraz 64 block)

This guide will assist you in setting up an IPv6 tunnel using Tunnelbroker.net. The steps provided are tested on Ubuntu (20.04 but works with other versions also).

## 1st step
`For this step, it is recommended to have knowledge of configuring and connecting to the instance using SSH or another protocol.` 

**If you do not have the knowledge yet, reffer to this [article](https://docs.oracle.com/en-us/iaas/Content/Compute/Tasks/launchinginstance.htm) or this [video](https://www.youtube.com/watch?v=89CVSTPjfCc).**
- Go to [cloud.oracle.com](https://cloud.oracle.com/)
- Create **new Instance (VM)**. Shape doesn't matter. *(I use Ampere A1.Flex)*
  
  **Note:** If you get an error `Out of the capacity`, there's nothing you can do but wait and try again in few days. It's frustrating, but it's Oracle.
- For a tested and correct configuration, it is recommended to set the Image to `Ubuntu`. As it was previously stated: *version doesn't matter*. Just be sure it's `>=20.04`.


## 2nd step
`Follow this step if your instance is already running.`
#### This step provides us with a possible server response from TunnelBroker, to which we will add our ip. This is discussed in the #4th step. 
- Go to the main page of your Instance and click on the `Subnet`.

![image](https://github.com/user-attachments/assets/26a3fd3e-0e15-43dc-a397-52874d0f5c3b)

- Click on the `Default Security List for your Virtual Cloud Network`.
  
![image](https://github.com/user-attachments/assets/d1224ea0-3fb9-4356-a753-7b768d895cf9)

- Click on `Add Ingress Rules`.
  
![image](https://github.com/user-attachments/assets/df0ee418-64a9-4b6b-9811-7145fe470973)
**Note**: As you can see, I already have the `ICMP IP Protocol` configured. But I will walk you through.
- The pop-up will show:
  
![image](https://github.com/user-attachments/assets/641c0883-9437-42fa-9d3a-49f5ac7330a3)

Configure it accordingly:
- For `Source CIDR` type: **0.0.0.0/0**
- For `IP Protocol` change to: **ICMP**
So your fully configured `Ingress Rule` should look like this:

![image](https://github.com/user-attachments/assets/ef467326-05e2-4f3a-ac30-ae33726b66f4)

- Click `Save changes`.


## Step 3

#### Enabling IPv6 Non-Local Bind

Before proceeding with the tunnel setup, it's recommended to enable IPv6 non-local bind. This allows applications to bind to non-local addresses.
On your configured and connected instance execute the following commands:
```bash
# Enable now
sysctl -w net.ipv6.ip_nonlocal_bind=1
# Persist for next boot
echo 'net.ipv6.ip_nonlocal_bind = 1' >> /etc/sysctl.conf
# (Optionally) Load in sysctl settings from the file specified or /etc/sysctl.conf if none given.
sysctl -p
```

## Step 4

### Register on Tunnelbroker.net:

1. Go to [Tunnelbroker.net](https://www.tunnelbroker.net).
2. Sign up for an account if you haven't already.
3. Log in to your account.

### Create a Tunnel:

1. After logging in, [Create Tunnel](https://tunnelbroker.net/new_tunnel.php).
2. Enter your IPv4 endpoint (your public IPv4 address).
- Can be obtained from the main instance page:
![image](https://github.com/user-attachments/assets/99c79cf0-2ea6-4fcb-82af-421d04df10da)
**Note**: It should already be *pingable* and Tunnelbroker should give us the info: `Possible Tunnel` or something like that. Green info message.
4. Select the nearest server to your location (physical, not the location of the instance).
- Fully configured tunnel should look like that:
![image](https://github.com/user-attachments/assets/aa682b33-b110-44f6-9525-ab0ca88aa467)

5. **Optionally**: Click on **Assign /48** to use `/48 block`. It is less likely to be blocked.
![image](https://github.com/user-attachments/assets/20f70ae5-bdb7-4566-83f9-7e171cd5e1d1)


### Configuration:
This step is done already at the server.

#### Command 1: Add IPv6 Tunnel Interface

```bash
sudo ip tunnel add he-ipv6 mode sit remote [TUNNEL_SERVER_IPV4] local [YOUR_CLIENT_IPV4] ttl 255
```

- Replace `[TUNNEL_SERVER_IPV4]` with the server's IPv4 address provided by Tunnelbroker.net.

![image](https://github.com/user-attachments/assets/e4ec08e3-134e-4828-9a60-668b1b9d5f8c)

- Replace `[YOUR_CLIENT_IPV4]` with your server's IPv4 address. We are using Oracle, so we need to replace it by using `Private IPv4 Address` obtained from the main page of the instance.
  
![image](https://github.com/user-attachments/assets/0c583a08-4a41-46a3-bb51-4c656a20aa28)


**Note**: If you encounter the "no buffer space available" error during this step due to misconfiguration, run:

```bash
sudo ip tun del he-ipv6
```
- Replace `he-ipv6` with the name of the interface you initially set up. You can check your current interfaces using `ifconfig`.

#### Command 2: Set up IPv6 Tunnel Interface

```bash
sudo ip link set he-ipv6 up
```

- This command brings the IPv6 tunnel interface up.

#### Command 3: Add IPv6 Address to the Tunnel Interface

For **/48** block:
```bash
sudo ip addr add [YOUR_IPV6_BLOCK]::2/48 dev he-ipv6
```

For **/64** block:
```bash
sudo ip addr add [YOUR_IPV6_BLOCK]::2/64 dev he-ipv6
```

- This assigns an IPv6 address to the tunnel interface.
- Replace `[YOUR_IPV6_BLOCK]` with your allocated **IPv6 block**. This one can be obtained from the Tunnelbroker tunnel which we previously created:

![image](https://github.com/user-attachments/assets/dfda7f3e-1c91-4378-b6fe-91a1f06ae7b5)

**Important note**: `Ipv6 block` isn't the whole address. It's the *block*. So for example:
- We have the `Routed /64` assigned as `2001:470:72:5e::/64`.
- The `block` we want to copy is: `2001:470:72:5e`.
- The same thing for `/48` one.


#### Command 4: Add IPv6 Default Route

```bash
sudo ip route add ::/0 via [YOUR_IPV6_BLOCK]::1 dev he-ipv6
```

- This command adds a default route for IPv6 traffic via the tunnel interface.
- If you encounter a **"File exists"** error, change `add` to `replace`
- Replace `[YOUR_IPV6_BLOCK]` with the previously mentioned block.

#### Command 5: Handle Limited Pingability

For **/48 block**:
```bash
sudo ip -6 route replace local [YOUR_IPV6_BLOCK]::/48 dev lo
```

For **/64 block**:
```bash
sudo ip -6 route replace local [YOUR_IPV6_BLOCK]::/64 dev lo
```

- This command ensures that traffic destined for addresses within your `/48` or `/64` block is routed correctly.

### Testing 

```bash
ping6 -I [YOUR_IPV6_BLOCK]::4 google.com
```
- This command pings `google.com` using the IPv6 address `[YOUR_IPV6_BLOCK]::4` as the source address.

```bash
ping6 -I [YOUR_IPV6_BLOCK]::3 google.com
```
- This command pings `google.com` using the IPv6 address `[YOUR_IPV6_BLOCK]::3` as the source address.

```bash
ping6 -I [YOUR_IPV6_BLOCK]::2 google.com
```
- This command pings `google.com` using the IPv6 address `[YOUR_IPV6_BLOCK]::2` as the source address.
- If the `ping6` using `::2` is the only one that works, refer to commands 4 and 5 for solution. 

These commands allow you to test connectivity to `google.com` using different IPv6 source addresses. Adjust the source addresses as needed for your testing purposes. 
