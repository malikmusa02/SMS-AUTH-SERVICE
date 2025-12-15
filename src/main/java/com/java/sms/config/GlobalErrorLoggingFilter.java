package com.java.sms.config;

import com.java.sms.model.ErrorLog;
import com.java.sms.model.User;
import com.java.sms.repository.UserRepository;
import com.java.sms.service.ErrorLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.Principal;

/**
 * Logs exceptions and non-2xx responses to DB. Runs after security filters (LOWEST_PRECEDENCE).
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalErrorLoggingFilter extends OncePerRequestFilter {

    private final ErrorLogService errorLogService;
    private final UserRepository userRepository;

    public GlobalErrorLoggingFilter(ErrorLogService errorLogService, UserRepository userRepository) {
        this.errorLogService = errorLogService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);

            int status = response.getStatus();
            if (status >= 400) {
                if (request.getAttribute("ERROR_LOGGED") == null) {
                    logAndSave(request, null, status, response);
                    request.setAttribute("ERROR_LOGGED", true);
                }
            }
        } catch (Exception ex) {
            if (request.getAttribute("ERROR_LOGGED") == null) {
                logAndSave(request, ex, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
                request.setAttribute("ERROR_LOGGED", true);
            }
            throw ex;
        }
    }

    private void logAndSave(HttpServletRequest request, Exception ex, int statusCode, HttpServletResponse response) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Principal principal = auth != null ? auth : request.getUserPrincipal();
            Long userId = PrincipalUtils.extractUserId(principal);

            User userEntity = null;
            if (userId != null) {
                userEntity = userRepository.findById(userId).orElse(null);
            }

            String errorType = ex != null ? ex.getClass().getName() : "HTTPError";
            String errorMessage = ex != null ? ex.getMessage() :
                    (response != null ? ("HTTP " + statusCode) : "HTTP error");
            String traceback = ex != null ? ExceptionUtils.stackTraceAsString(ex) : "";

            ErrorLog e = new ErrorLog();
            e.setUser(userEntity);
            e.setEndpoint(request.getRequestURI());
            e.setMethod(request.getMethod());
            e.setStatusCode(statusCode);
            e.setErrorType(errorType);
            e.setErrorMessage(errorMessage);
            e.setTracebackInfo(traceback);

            errorLogService.save(e);
            log.info("Logged error {} at {} (userId={})", statusCode, request.getRequestURI(), userId);
        } catch (Exception e) {
            log.error("Failed to log error in filter: {}", e.getMessage(), e);
        }
    }
}
