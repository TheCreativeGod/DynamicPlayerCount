package com.thecreativegod.dynamicplayercount;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigurationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationHandler.class);
    private static final Path CONFIG_PATH = Paths.get("plugins/DynamicPlayerCount/config.yml");

    private List<ServerConfig> servers;
    private boolean debug;

    public ConfigurationHandler() {
        createDefaultConfig();
        loadConfig();
    }

    private void createDefaultConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (in == null) {
                    throw new IOException("Default config file not found in resources");
                }
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.copy(in, CONFIG_PATH);
            } catch (IOException e) {
                logger.error("Unable to create default configuration", e);
            }
        }
    }

    private void loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            Config config = mapper.readValue(CONFIG_PATH.toFile(), Config.class);
            servers = config.getServers();
            debug = config.isDebug();

            // Log the server mappings during configuration loading (always)
            servers.forEach(server ->
                logger.info("Mapping virtual host " + server.getHost() + " to server " + server.getServerName()));
        } catch (IOException e) {
            logger.error("Unable to load configuration", e);
        }
    }

    public List<ServerConfig> getServers() {
        return servers;
    }

    public boolean isDebug() {
        return debug;
    }

    public static class Config {
        private List<ServerConfig> servers;
        private boolean debug;

        public List<ServerConfig> getServers() {
            return servers;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    public static class ServerConfig {
        private String host;
        private String serverName;
        private boolean maxPlayerCount;
        private String type;
        private int maxPlayers;

        public String getHost() {
            return host;
        }

        public String getServerName() {
            return serverName;
        }

        public boolean isMaxPlayerCount() {
            return maxPlayerCount;
        }

        public String getType() {
            return type;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }
    }
}
