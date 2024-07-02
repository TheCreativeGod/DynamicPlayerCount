Changes the Player Count on the Server List when pinged based on what host in pinged in your Velocity network.

You need to map `config.yml` to match the servers from your network where you want to list a different Player Count.

Example configuration (old configuration for 0.2 and older versions at the bottom):

```
servers:
  - host: "play.server1.com"
    serverName: "server1"
  - host: "play.server2.com"
    serverName: "server2"
debug: false
```
`host` is the host being pinged, and `serverName` is the server in your network where you want to take the player count. If a host is not mapped here, it will follow your Velocity configuration for player count (shows players in your entire network by default).
### Old configuration (for 0.2 and older versions)
```
serverMappings:
  - virtualHost: "lobby.server1.com"
    serverName: "lobby"
  - virtualHost: "pvp.server2.com"
    serverName: "pvp"
loggingEnabled: false
```
**This is a work in progress and has not been tested using different plugins setups.**
