package com.example.crossserver;

import com.example.crossserver.config.PluginConfig;
import com.example.crossserver.listener.LoginListener;
import com.example.crossserver.listener.ServerForwardListener;
import com.example.crossserver.session.SessionManager;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * FreshWaterLandfallX 主类。
 *
 * <p>在 Velocity(legacy 转发) + Waterfall 嵌套架构下，于登录阶段读取已解析的真实玩家信息，
 * 执行 UUID 白名单 / IP 黑名单校验，并在玩家连入子服时将真实信息转发给子服。</p>
 */
public final class CrossServerPlugin extends Plugin {

    private final SessionManager sessionManager = new SessionManager();
    private PluginConfig pluginConfig;

    @Override
    public void onEnable() {
        reloadConfig();

        getProxy().getPluginManager().registerListener(this, new LoginListener(this));
        getProxy().getPluginManager().registerListener(this, new ServerForwardListener(this));

        getProxy().registerChannel(pluginConfig.getChannel());

        getLogger().info("FreshWaterLandfallX 已启用，转发通道: " + pluginConfig.getChannel());
    }

    @Override
    public void onDisable() {
        getLogger().info("FreshWaterLandfallX 已禁用。");
    }

    /**
     * 重新加载 config.yml（首次启用时会从 jar 内复制默认配置）。
     */
    public void reloadConfig() {
        try {
            Configuration configuration = loadConfiguration();
            this.pluginConfig = PluginConfig.from(configuration);
        } catch (IOException e) {
            getLogger().severe("加载 config.yml 失败: " + e.getMessage());
            // 退回到内置默认值，保证插件可用
            this.pluginConfig = PluginConfig.from(new Configuration());
        }
    }

    private Configuration loadConfiguration() throws IOException {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            throw new IOException("无法创建数据目录: " + getDataFolder());
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                if (in == null) {
                    throw new IOException("jar 内缺少默认 config.yml 资源");
                }
                Files.copy(in, configFile.toPath());
            }
        }

        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
