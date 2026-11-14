# Apple High Performance through forwarding gateways

Standard VNC carries framebuffer updates and control on TCP, normally port 5900. Apple's High Performance video/audio additionally use independent UDP sockets. A working cursor or a successful TCP login is insufficient to verify media reachability.

The client reads the media ports announced by the Mac in `internal/rfb/media/reply.go`. `internal/rfb/media/socket.go:dialLeg` uses the TCP connection's local/remote addresses and binds the same UDP port on both endpoints. Native Mac software similarly expects the media endpoint to match its TCP peer. Apple's current documentation lists UDP 5900, 5901 and 5902; the exact flows in use depend on the negotiated session.

## Lucky and similar socket proxies

Lucky's UDP socket proxy uses `net.DialUDP("udp", nil, target)` for the backend connection (`module/portforward/socketproxy/udpproxy.go`). The OS chooses an ephemeral source port. This is ordinary UDP proxy behavior, but it does not preserve Apple's equal-port media endpoint semantics. TCP forwarding also makes the gateway, rather than the Android device, the Mac's TCP peer. Enabling additional UDP forwarding rules alone therefore does not establish compatibility.

For the full native High Performance path, prefer a directly reachable Mac address or a routed VPN that supports UDP between the phone and the Mac. A purpose-built relay would need to preserve the negotiated media endpoints; arbitrary TCP forwarding is not enough. Confirm a proposed gateway setup with a packet capture rather than assuming that an open port fixes the return path.

## Diagnose

The fork logs the negotiated video/audio UDP ports and TCP endpoints under the Android `libsharing` log tag. These logs contain network addresses, so redact them before posting publicly. They never intentionally log credentials.

On the Mac, read-only checks include:

```sh
netstat -anv -p tcp
netstat -anv -p udp
```

Filter for `screensharingd` / `avconferenced` and the media ports. A privileged packet capture during a fresh external connection can distinguish no media, one-way traffic, and traffic that reaches the Android device but fails to decode. Do not interrupt someone else's active screen-sharing session during diagnosis.

## Sources

- [Apple High Performance requirements](https://support.apple.com/guide/remote-desktop/apdf8e09f5a9/mac)
- [Lucky UDP proxy](https://github.com/gdy666/lucky/blob/master/module/portforward/socketproxy/udpproxy.go)
- [Lucky port-forwarding documentation](https://www.lucky666.cn/docs/modules/portforward/)
