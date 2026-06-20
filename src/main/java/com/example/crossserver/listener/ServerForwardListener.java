package com.example.crossserver.listener;

import com.example.crossserver.CrossServerPlugin;
import com.example.crossserver.config.PluginConfig;
import com.example.crossserver.session.PlayerSession;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 玩家连入子服时，把缓存的真实信息通过插件消息通道转发给该子服。
 *
 * <p>消息格式（DataOutputStream / writeUTF，与 Minecraft 插件消息一致）：
 * <pre>
 *   UTF  subChannel = "LOGIN_DATA"
 *   UTF  name
 *   UTF  uuid
 *   UTF  ip
 * </pre>
 * 子服注册同名通道后用 DataInputStream 按相同顺序读取即可。</p>
 */
public final class ServerForwardListener implements Listener {

    private final CrossServerPlugin plugin;

    public ServerForwardListener(CrossServerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        PluginConfig config = plugin.getPluginConfig();
        if (!config.isForwardEnabled()) {
            return;
        }

        PlayerSession session = plugin.getSessionManager().get(event.getPlayer().getUniqueId());
        if (session == null) {
            return;
        }

        byte[] payload = encode(session);
        if (payload == null) {
            return;
        }

        Server server = event.getServer();
        server.sendData(config.getChannel(), payload);
    }

    private byte[] encode(PlayerSession session) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(byteStream)) {
            out.writeUTF("LOGIN_DATA");
            out.writeUTF(session.name());
            out.writeUTF(session.uuid().toString());
            out.writeUTF(session.ip() == null ? "" : session.ip());
        } catch (IOException e) {
            plugin.getLogger().warning("编码转发数据失败: " + e.getMessage());
            return null;
        }
        return byteStream.toByteArray();
    }
}
