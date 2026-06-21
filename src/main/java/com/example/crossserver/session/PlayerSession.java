package com.example.crossserver.session;

import java.util.UUID;

/**
 * 登录校验通过后缓存的玩家真实信息（不可变）。
 *
 * <p>访问器命名沿用 record 风格（name()/uuid()/ip()/loginTime()），以兼容 Java 8。</p>
 */
public final class PlayerSession {

    private final String name;
    private final UUID uuid;
    private final String ip;
    private final long loginTime;

    public PlayerSession(String name, UUID uuid, String ip, long loginTime) {
        this.name = name;
        this.uuid = uuid;
        this.ip = ip;
        this.loginTime = loginTime;
    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }

    public String ip() {
        return ip;
    }

    public long loginTime() {
        return loginTime;
    }
}
