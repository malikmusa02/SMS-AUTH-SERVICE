package com.java.sms.security;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtBlacklistService {

    // token -> expiry mapping
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expiryTime) {
        blacklist.put(token, expiryTime);
    }

    public boolean isBlacklisted(String token) {
        Long expiry = blacklist.get(token);

        if (expiry == null) {
            return false;
        }

        // agar token expire ho gaya hai toh blacklist se hata do
        if (expiry < System.currentTimeMillis()) {
            blacklist.remove(token);
            return false;
        }

        return true;
    }
}

