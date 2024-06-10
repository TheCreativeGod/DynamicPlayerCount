Changes the Player Count on the Server List when pinged based on what host in pinged in your Velocity network.

You need to map `config.yml` to match the servers from your network where you want to list a different Player Count.

Example configuration:

```
serverMappings:
  - virtualHost: "lobby.server1.com"
    serverName: "lobby"
  - virtualHost: "pvp.server2.com"
    serverName: "pvp"

```

# This is a work in progress and has not been tested using different plugins setups