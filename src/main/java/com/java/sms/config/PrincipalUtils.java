package com.java.sms.config;

import org.springframework.security.core.Authentication;

import java.security.Principal;

/**
 * Extract user id from Principal / Authentication safely (reflection fallback).
 * Works when your JwtAuthenticationFilter sets the User entity as the principal.
 */
public final class PrincipalUtils {
    private PrincipalUtils() {}

    public static Long extractUserId(Principal principalOrAuth) {
        if (principalOrAuth == null) return null;
        try {
            if (principalOrAuth instanceof Authentication auth) {
                Object p = auth.getPrincipal();
                Long id = tryExtractIdFromObject(p);
                if (id != null) return id;
                Long maybe = tryUsernameToId(p);
                if (maybe != null) return maybe;
            }
            return tryExtractIdFromObject(principalOrAuth);
        } catch (Exception e) {
            return null;
        }
    }

    private static Long tryExtractIdFromObject(Object obj) {
        if (obj == null) return null;
        try {
            var m = obj.getClass().getMethod("getId");
            Object id = m.invoke(obj);
            if (id instanceof Number) return ((Number) id).longValue();
            if (id != null) {
                try { return Long.parseLong(id.toString()); } catch (NumberFormatException ignore) {}
            }
        } catch (NoSuchMethodException ignore) {}
        catch (Exception ignore) {}
        return null;
    }

    private static Long tryUsernameToId(Object principalObj) {
        try {
            var mu = principalObj.getClass().getMethod("getUsername");
            Object username = mu.invoke(principalObj);
            if (username != null) {
                try { return Long.parseLong(username.toString()); } catch (NumberFormatException ignore) {}
            }
        } catch (NoSuchMethodException ignore) {}
        catch (Exception ignore) {}
        return null;
    }
}
