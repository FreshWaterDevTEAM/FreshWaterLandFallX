package com.example.crossserver.listener;

import com.example.crossserver.CrossServerPlugin;
import com.example.crossserver.config.PluginConfig;
import com.example.crossserver.session.PlayerSession;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * 登录监听：在 LoginEvent 阶段（连接信息已由 legacy 转发解析）执行白名单 / 黑名单校验，
 * 校验通过则缓存玩家真实信息供后续转发使用。
 */
public final class LoginListener implements Listener {

    private final CrossServerPlugin plugin;

    public LoginListener(CrossServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        PendingConnection connection = event.getConnection();

        String name = connection.getName();
        UUID uuid = connection.getUniqueId();
        String ip = resolveIp(connection);

        plugin.getLogger().info("玩家 " + name + " 登录，真实IP: " + ip + ", UUID: " + uuid);

        if (config.isWhitelistEnabled() && (uuid == null || !config.isUuidAllowed(uuid.toString()))) {
            event.setCancelled(true);
            event.setReason(reason("§c你的 UUID 不在白名单中"));
            plugin.getLogger().info("拒绝 " + name + "：UUID 不在白名单中。");
            return;
        }

        if (ip != null && config.isIpBanned(ip)) {
            event.setCancelled(true);
            event.setReason(reason("§c你的 IP 已被禁止"));
            plugin.getLogger().info("拒绝 " + name + "：IP " + ip + " 已被封禁。");
            return;
        }

        if (uuid != null) {
            plugin.getSessionManager().put(new PlayerSession(name, uuid, ip, System.currentTimeMillis()));
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        plugin.getSessionManager().remove(event.getPlayer().getUniqueId());
    }

    private static TextComponent reason(String legacyText) {
        return new TextComponent(TextComponent.fromLegacyText(legacyText));
    }

    private static String resolveIp(PendingConnection connection) {
        InetSocketAddress address = (InetSocketAddress) connection.getSocketAddress();
        if (address != null) {
            return address.getHostString();
        }
        return null;
    }
}
