package com.example.crossserver.session;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 以 UUID 为键缓存玩家会话，登录时写入，断开时移除。
 */
public final class SessionManager {

    private final ConcurrentHashMap<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();

    public void put(PlayerSession session) {
        sessions.put(session.uuid(), session);
    }

    public PlayerSession get(UUID uuid) {
        return sessions.get(uuid);
    }

    public void remove(UUID uuid) {
        sessions.remove(uuid);
    }

    public int size() {
        return sessions.size();
    }
}
