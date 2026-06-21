# FreshWaterLandfallX

Waterfall（BungeeCord API 兼容）跨服登录监听插件。用于 **Velocity 前置代理（legacy 转发）+ Waterfall 子代理** 的嵌套架构：在登录阶段读取已解析的真实玩家信息，执行 UUID 白名单 / IP 黑名单校验，并在玩家连入子服时把真实信息转发给子服。

## 架构

```
玩家 -> Velocity(前置, legacy 转发) -> Waterfall(本插件) -> 子服(Paper/Spigot)
```

- `LoginEvent` 阶段：连接信息（玩家名 / UUID / 真实 IP）已被 Waterfall 解析完成，此时做校验与缓存。
- `ServerConnectedEvent` 阶段：玩家进入某个子服，向该子服通过插件消息通道发送真实信息。

## 编译

需要 JDK 17+ 与 Maven。

```bash
mvn clean package
```

产物位于 `target/FreshWaterLandfallX-1.0.1.jar`。

> 说明：插件以 Java 8 字节码编译（`maven.compiler.release=8`），兼容运行在 Java 8 及以上的 Waterfall/BungeeCord 服务器。

## 部署

1. 将打包好的 jar 放入 Waterfall 的 `plugins/` 目录。
2. 启动一次以生成默认配置 `plugins/FreshWaterLandfallX/config.yml`。
3. 按需编辑配置后重启代理。

## 配置说明（config.yml）

```yaml
# 是否开启 UUID 白名单校验
whitelist-enabled: true

# 允许登录的玩家 UUID（标准带横线格式）
allowed-uuids:
  - "069a79f4-44e9-4726-a5be-fca90e38aaf5"

# 被封禁的真实 IP
banned-ips:
  - "1.2.3.4"

# 将玩家真实信息转发给子服
forward:
  enabled: true
  channel: "crossserver:login"
```

- `whitelist-enabled: false` 时不校验白名单，但仍会执行 IP 黑名单校验。
- UUID 比较大小写不敏感。

## 子服接收端示例（Paper / Spigot）

转发的消息格式为 `DataOutputStream.writeUTF` 顺序写入：`subChannel`、`name`、`uuid`、`ip`。子服注册同名通道后即可读取：

```java
public final class LoginDataReceiver extends JavaPlugin implements PluginMessageListener {

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, "crossserver:login", this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!"crossserver:login".equals(channel)) {
            return;
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in.readUTF();
            if (!"LOGIN_DATA".equals(subChannel)) {
                return;
            }
            String name = in.readUTF();
            String uuid = in.readUTF();
            String ip = in.readUTF();
            getLogger().info("收到真实信息: " + name + " / " + uuid + " / " + ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

> 注意：插件消息通道需要该子服上至少有一名玩家在线才能传递。本插件在 `ServerConnectedEvent` 时发送，玩家此时已连入目标子服，满足条件。
