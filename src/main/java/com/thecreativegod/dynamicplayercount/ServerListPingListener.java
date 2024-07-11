package com.thecreativegod.dynamicplayercount;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServerListPingListener {

    private static final Logger logger = LoggerFactory.getLogger(ServerListPingListener.class);
    private final ProxyServer server;
    private final Map<String, ConfigurationHandler.ServerConfig> serverMappings;
    private final boolean debug;

    public ServerListPingListener(ProxyServer server, ConfigurationHandler configHandler) {
        this.server = server;
        this.serverMappings = configHandler.getServers().stream()
            .collect(Collectors.toMap(ConfigurationHandler.ServerConfig::getHost, serverConfig -> serverConfig));
        this.debug = configHandler.isDebug();
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        Optional<InetSocketAddress> virtualHost = event.getConnection().getVirtualHost();
        if (!virtualHost.isPresent()) {
            log("Player virtual host not present.");
            return;
        }

        String hostString = virtualHost.get().getHostString();
        log("Received ping for host: " + hostString);
        Optional<RegisteredServer> targetServer = determineTargetServer(hostString);

        if (targetServer.isPresent()) {
            int playerCount = targetServer.get().getPlayersConnected().size();
            log("Setting player count for " + targetServer.get().getServerInfo().getName() + ": " + playerCount);
            ServerPing.Builder pingBuilder = event.getPing().asBuilder();

            ConfigurationHandler.ServerConfig serverConfig = serverMappings.get(hostString.toLowerCase());
            if (serverConfig != null && serverConfig.isMaxPlayerCount()) {
                switch (serverConfig.getType().toLowerCase()) {
                    case "dynamic":
                        pingBuilder.maximumPlayers(playerCount + 1);
                        break;
                    case "static":
                        pingBuilder.maximumPlayers(serverConfig.getMaxPlayers());
                        break;
                    default:
                        log("Unknown type for max player count: " + serverConfig.getType());
                }
            }

            pingBuilder.onlinePlayers(playerCount);
            event.setPing(pingBuilder.build());
        } else {
            log("No target server found for host: " + hostString);
        }
    }

    private Optional<RegisteredServer> determineTargetServer(String virtualHost) {
        ConfigurationHandler.ServerConfig serverConfig = serverMappings.get(virtualHost.toLowerCase());
        return serverConfig != null ? server.getServer(serverConfig.getServerName()) : Optional.empty();
    }

    private void log(String message) {
        if (debug) {
            logger.info(message);
        }
    }
}
