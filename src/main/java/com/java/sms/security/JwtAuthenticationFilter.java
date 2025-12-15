package com.java.sms.security;

import com.java.sms.exception.InvalidTokenException;
import com.java.sms.model.User;
import com.java.sms.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;



@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final JwtBlacklistService jwtBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, JwtBlacklistService jwtBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;

        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
         String jwt = null;
         String userEmail = null;

        // Check for Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        String tokenType = jwtUtil.extractTokenType(jwt);
        if (!"accessToken".equalsIgnoreCase(tokenType)) {
            throw new InvalidTokenException("Only access tokens are allowed for this request.", HttpStatus.FORBIDDEN);
        }



        //  check blacklist before doing anything
        if (jwtBlacklistService.isBlacklisted(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token is invalid (logged out). Please login again.");
            return;
        }


        userEmail = jwtUtil.extractEmail(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String finalUserEmail = userEmail;
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found: " + finalUserEmail));


            // Block inactive users
            if (!Boolean.TRUE.equals(user.getActive())) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"User account is inactive. Please contact admin.\"}");
                return;
            }


            if (jwtUtil.isTokenValid(jwt, userEmail)) {

                List<String> roles = jwtUtil.extractRoles(jwt);

                var authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);


                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

            }

//            if (user != null && jwtUtil.isTokenValid(jwt, userEmail)) {
//
//                //  Extract roles from JWT
//                List<String> roles = jwtUtil.extractRoles(jwt);
//
//                var authorities = roles.stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) //  prefix
//                        .toList();
//
//                UsernamePasswordAuthenticationToken authToken =
//                        new UsernamePasswordAuthenticationToken(user, null, authorities);
//
//                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//
//
//            }
        }
        filterChain.doFilter(request, response);
    }
}