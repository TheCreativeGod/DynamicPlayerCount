package com.thecreativegod.dynamicplayercount;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = "dynamicplayercount", name = "Dynamic Player Count", version = "1.0", description = "Dynamically changes player count based on pinged host, with option to set Max Player count", authors = "TheCreativeGod")
public class DynamicPlayerCountPlugin {

    private final ProxyServer server;
    private ConfigurationHandler configHandler;

    @Inject
    public DynamicPlayerCountPlugin(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configHandler = new ConfigurationHandler();
        server.getEventManager().register(this, new ServerListPingListener(server, configHandler));
    }
}