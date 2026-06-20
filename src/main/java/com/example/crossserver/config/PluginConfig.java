package com.example.crossserver.config;

import net.md_5.bungee.config.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 持有从 config.yml 读取的配置数据的不可变快照。
 */
public final class PluginConfig {

    private final boolean whitelistEnabled;
    private final Set<String> allowedUuids;
    private final Set<String> bannedIps;
    private final boolean forwardEnabled;
    private final String channel;

    private PluginConfig(boolean whitelistEnabled, Set<String> allowedUuids, Set<String> bannedIps,
                         boolean forwardEnabled, String channel) {
        this.whitelistEnabled = whitelistEnabled;
        this.allowedUuids = allowedUuids;
        this.bannedIps = bannedIps;
        this.forwardEnabled = forwardEnabled;
        this.channel = channel;
    }

    /**
     * 从一个已加载的 BungeeCord Configuration 解析出 PluginConfig。
     */
    public static PluginConfig from(Configuration configuration) {
        boolean whitelistEnabled = configuration.getBoolean("whitelist-enabled", true);

        Set<String> allowedUuids = toLowerSet(configuration.getStringList("allowed-uuids"));
        Set<String> bannedIps = new HashSet<>(configuration.getStringList("banned-ips"));

        boolean forwardEnabled = configuration.getBoolean("forward.enabled", true);
        String channel = configuration.getString("forward.channel", "crossserver:login");

        return new PluginConfig(whitelistEnabled, allowedUuids, bannedIps, forwardEnabled, channel);
    }

    private static Set<String> toLowerSet(List<String> list) {
        Set<String> set = new HashSet<>();
        for (String value : list) {
            if (value != null) {
                set.add(value.trim().toLowerCase());
            }
        }
        return set;
    }

    public boolean isWhitelistEnabled() {
        return whitelistEnabled;
    }

    /**
     * UUID 是否在白名单内（大小写不敏感）。
     */
    public boolean isUuidAllowed(String uuid) {
        return allowedUuids.contains(uuid.toLowerCase());
    }

    /**
     * IP 是否被封禁。
     */
    public boolean isIpBanned(String ip) {
        return bannedIps.contains(ip);
    }

    public boolean isForwardEnabled() {
        return forwardEnabled;
    }

    public String getChannel() {
        return channel;
    }
}
