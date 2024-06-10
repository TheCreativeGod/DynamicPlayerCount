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
import java.io.File;
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

@Plugin(id = "dynamicplayercount", name = "Dynamic Player Count", version = "1.0", description = "Dynamically changes player count based on URL")
public class DynamicPlayerCountPlugin {

    private static final Logger logger = LoggerFactory.getLogger(DynamicPlayerCountPlugin.class);
    private final ProxyServer server;
    private Map<String, String> serverMappings;

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
                    throw new IOException("Resource 'config.yml' not found");
                }
                Files.createDirectories(configPath.getParent());
                Files.copy(in, configPath);
                logger.info("Default config.yml created successfully.");
            } catch (IOException e) {
                logger.error("Failed to create default config.yml", e);
            }
        }
    }

    private void loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Config config = mapper.readValue(new File("plugins/DynamicPlayerCount/config.yml"), Config.class);
            serverMappings = config.getServerMappings().stream()
                .collect(Collectors.toMap(Config.ServerMapping::getVirtualHost, Config.ServerMapping::getServerName));
            logger.info("Config loaded successfully: " + serverMappings);
        } catch (IOException e) {
            logger.error("Failed to load config.yml", e);
        }
    }

    private static class Config {
        private List<ServerMapping> serverMappings;

        public List<ServerMapping> getServerMappings() {
            return serverMappings;
        }

        public static class ServerMapping {
            private String virtualHost;
            private String serverName;

            public String getVirtualHost() {
                return virtualHost;
            }

            public String getServerName() {
                return serverName;
            }
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
                logger.warn("Player virtual host not present.");
                return;
            }

            String hostString = virtualHost.get().getHostString();
            logger.info("Received ping for host: " + hostString);
            Optional<RegisteredServer> targetServer = determineTargetServer(hostString);

            if (targetServer.isPresent()) {
                int playerCount = targetServer.get().getPlayersConnected().size();
                logger.info("Setting player count for " + targetServer.get().getServerInfo().getName() + ": " + playerCount);
                ServerPing.Builder pingBuilder = event.getPing().asBuilder();
                pingBuilder.onlinePlayers(playerCount);
                event.setPing(pingBuilder.build());
            } else {
                logger.warn("No target server found for host: " + hostString);
            }
        }

        private Optional<RegisteredServer> determineTargetServer(String virtualHost) {
            String serverName = serverMappings.get(virtualHost.toLowerCase());
            logger.info("Mapping virtual host " + virtualHost + " to server " + serverName);
            return serverName != null ? server.getServer(serverName) : Optional.empty();
        }
    }
}
