# Dynamic Player Count

Changes the Player Count on the Server List when pinged based on what host in pinged in your Velocity network, with posibility to set a max player count.

You need to map `config.yml` to match the servers from your network where you want to list a different Player Count.

Example configuration:

```
servers:
  - host: "play.server1.com"
    serverName: "server1"
    maxPlayerCount: false
    type: "dynamic"
    maxPlayers: 100
  - host: "play.server2.com"
    serverName: "server2"
    maxPlayerCount: false
    type: "static"
    maxPlayers: 50
debug: false
```
`host` is the host being pinged, and `serverName` is the server in your network where you want to take the player count. If a host is not mapped here, it will follow your Velocity configuration for player count (shows players in your entire network by default).

`type` is ignored if `maxPlayerCount` is set to false, and `maxPlayers` is ignored if `type` is set to `dynamic`

`static` sets the max player amount in your host to the `maxPlayers` number, while `dynamic` changes it to current players +1 (planning to add more functionality on the `dynamic` type)

**This plugin has not been tested using different plugins setups. Please submit an issue if you encounter any problem with another plugins or in general**
