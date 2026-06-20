package com.example.crossserver.session;

import java.util.UUID;

/**
 * 登录校验通过后缓存的玩家真实信息（不可变）。
 *
 * @param name      玩家名
 * @param uuid      玩家 UUID
 * @param ip        真实 IP（已由 legacy 转发解析）
 * @param loginTime 登录时间戳（毫秒）
 */
public record PlayerSession(String name, UUID uuid, String ip, long loginTime) {
}
