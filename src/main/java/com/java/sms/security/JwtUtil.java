package com.java.sms.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60;          // 1 hour
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    // Generate secure signing key
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser() //  NEW API (no parserBuilder)
                .verifyWith(getSigningKey()) // verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    // Generate Access Token (with roles)
    public String generateAccessToken(String email, List<String> roleNames) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roleNames)
                .claim("type", "accessToken")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    //  Generate Refresh Token
    public String generateRefreshToken(String email, List<String> roleNames) {
        return Jwts.builder()
                .subject(email)
                .claim("roles", roleNames)
                .claim("type", "refreshToken")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    //  Extract email (subject)
    public String extractEmail(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    // Extract roles list
    public List<String> extractRoles(String token) {
        try {
            Object roles = extractAllClaims(token).get("roles");
            if (roles instanceof List<?> roleList) {
                return roleList.stream()
                        .map(Object::toString)
                        .toList();
            }
        } catch (JwtException ignored) {}
        return List.of();
    }

    //  Extract token type ("accessToken" / "refreshToken")
    public String extractTokenType(String token) {
        try {
            return extractAllClaims(token).get("type", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    // Check token expiration
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            return true; // consider invalid/expired if parse fails
        }
    }

    // Validate token (email match + not expired)
    public boolean isTokenValid(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return (tokenEmail != null && tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    // Validate refresh token
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String type = claims.get("type", String.class);
            Date expiration = claims.getExpiration();
            return "refreshToken".equals(type) && expiration.after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    // Get token expiry timestamp (milliseconds)
    public long getExpiry(String token) {
        try {
            return extractAllClaims(token).getExpiration().getTime();
        } catch (JwtException e) {
            return 0L;
        }
    }

    // (Optional) For debugging - print all claims
    public void printTokenDetails(String token) {
        try {
            Claims claims = extractAllClaims(token);
            System.out.println("Subject: " + claims.getSubject());
            System.out.println("Roles: " + claims.get("roles"));
            System.out.println("Type: " + claims.get("type"));
            System.out.println("Expiration: " + claims.getExpiration());
        } catch (JwtException e) {
            System.out.println("Invalid token: " + e.getMessage());
        }
    }
}














//package com.java.sms.security;
//
//import com.java.sms.model.Role;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.List;
//import java.util.Set;
//
//
//@Component
//public class JwtUtil {
//
////    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey123"; // at least 256-bit
//
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1 hour
//    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days
//
//
//    private SecretKey getSigningKey() {
//        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
//    }
//
//
//
//    // with role generate a token
////    public String generateAccessToken(String email, List<Role> roles) {
////        List<String> roleNames = roles.stream()
////                .map(Role::getName) // Role::getName if it's entity
////                .toList();
////
////        return Jwts.builder()
////                .subject(email)
////                .claim("roles", roleNames) //  store list of roles
////                .claim("type","accessToken")
////                .issuedAt(new Date(System.currentTimeMillis()))
////                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
////                .signWith(getSigningKey())
////                .compact();
////    }
//
//
//    public String generateAccessToken(String email, List<String> roleNames) {
//
//        return Jwts.builder()
//                .subject(email)
//                .claim("roles", roleNames)
//                .claim("type", "accessToken")
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//
//    public String generateRefreshToken(String email, List<String> roleNames) {
//
//        return Jwts.builder()
//                .subject(email)
//                .claim("roles", roleNames)
//                .claim("type", "refreshToken")
//                .issuedAt(new Date(System.currentTimeMillis()))
//                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
//                .signWith(getSigningKey())
//                .compact();
//    }
//
//
//    public String extractTokenType(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .verifyWith(getSigningKey())
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            // Extract the "type" claim (you set this as "accessToken" or "refreshToken")
//            return claims.get("type", String.class);
//        } catch (JwtException | IllegalArgumentException e) {
//            return null;
//        }
//    }
//
//
//
//    public boolean validateRefreshToken(String token) {
//        try {
//            Claims claims = Jwts.parser()
//                    .verifyWith(getSigningKey())
//                    .build()
//                    .parseSignedClaims(token)
//                    .getPayload();
//
//            String type = claims.get("type", String.class);
//            Date expiration = claims.getExpiration();
//
//            return "refreshToken".equals(type) && expiration.after(new Date());
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//
//    public String extractEmail(String token) {
//        return Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//    }
//
//    public boolean isTokenValid(String token, String email) {
//        return email.equals(extractEmail(token)) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        Date expiration = Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getExpiration();
//        return expiration.before(new Date());
//    }
//
//
//    // inside JwtUtil
//    public List<String> extractRoles(String token) {
//        Claims claims = Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//
//        Object roles = claims.get("roles");
//
//        if (roles instanceof List<?> roleList) {
//            return roleList.stream()
//                    .map(Object::toString)
//                    .toList();
//        }
//        return List.of();
//    }
//
//
//    // Get expiry time in milliseconds
//    public long getExpiry(String token) {
//        Date expiration = Jwts.parser()
//                .verifyWith(getSigningKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getExpiration();
//
//        return expiration.getTime();
//    }
//
//
//}