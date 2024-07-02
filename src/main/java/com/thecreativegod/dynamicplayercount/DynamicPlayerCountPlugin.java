package com.thecreativegod.dynamicplayercount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin(id = "dynamicplayercount", name = "Dynamic Player Count", version = "0.3-SNAPSHOT", description = "Dynamically changes player count based on URL", authors = "TheCreativeGod")
public class DynamicPlayerCountPlugin {

    private static final Logger logger = LoggerFactory.getLogger(DynamicPlayerCountPlugin.class);
    private final ProxyServer server;
    private Map<String, String> serverMappings;
    private boolean debug;

    @Inject
    public DynamicPlayerCountPlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        createDefaultConfig();
        loadConfig();
        server.getEventManager().register(this, new ServerListPingListener(server, serverMappings));
    }

    private void createDefaultConfig() {
        Path configPath = Paths.get("plugins/DynamicPlayerCount/config.yml");
        if (!Files.exists(configPath)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    throw new IOException("Default config file not found");
                }
                Files.createDirectories(configPath.getParent());
                Files.copy(in, configPath);
                logger.info("Default config.yml created successfully.");
            } catch (IOException e) {
                logger.error("Unable to create default configuration", e);
            }
        }
    }

    private void loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Path configPath = Paths.get("plugins/DynamicPlayerCount/config.yml");
        try {
            Config config = mapper.readValue(configPath.toFile(), Config.class);
            serverMappings = config.getServers().stream()
                .collect(Collectors.toMap(ServerMapping::getHost, ServerMapping::getServerName));
            debug = config.isDebug();
            
            // Log the server mappings during configuration loading (always)
            serverMappings.forEach((host, serverName) -> 
                logger.info("Mapping virtual host " + host + " to server " + serverName));
        } catch (IOException e) {
            logger.error("Unable to load configuration", e);
        }
    }

    public static class Config {
        private List<ServerMapping> servers;
        private boolean debug;

        public List<ServerMapping> getServers() {
            return servers;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    public static class ServerMapping {
        private String host;
        private String serverName;

        public String getHost() {
            return host;
        }

        public String getServerName() {
            return serverName;
        }
    }

    public class ServerListPingListener {

        private final ProxyServer server;
        private final Map<String, String> serverMappings;

        ServerListPingListener(ProxyServer server, Map<String, String> serverMappings) {
            this.server = server;
            this.serverMappings = serverMappings;
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
                pingBuilder.onlinePlayers(playerCount);
                event.setPing(pingBuilder.build());
            } else {
                log("No target server found for host: " + hostString);
            }
        }

        private Optional<RegisteredServer> determineTargetServer(String virtualHost) {
            String serverName = serverMappings.get(virtualHost.toLowerCase());
            return serverName != null ? server.getServer(serverName) : Optional.empty();
        }

        private void log(String message) {
            if (debug) {
                logger.info(message);
            }
        }
    }
}
