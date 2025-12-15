package com.java.sms.exception;


import com.java.sms.config.ExceptionUtils;
import com.java.sms.config.PrincipalUtils;
import com.java.sms.model.ErrorLog;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import com.java.sms.model.User;
import com.java.sms.repository.UserRepository;
import com.java.sms.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorLogService errorLogService;
    private final UserRepository userRepository;


    public GlobalExceptionHandler(ErrorLogService errorLogService, UserRepository userRepository) {
        this.errorLogService = errorLogService;
        this.userRepository = userRepository;
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> handleInvalidToken(InvalidTokenException ex) {

        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("error", ex.getMessage(), "status", ex.getStatus().value()));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason()));
    }




    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, HttpServletRequest request) {
        // Log and return the ApiException's status + message
        safeLogException(ex, request, ex.getStatus().value());
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }




    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
        safeLogException(ex, request, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }



    private void safeLogException(Exception ex, HttpServletRequest request, int statusCode) {

        try {
            if (request.getAttribute("ERROR_LOGGED") != null) return; // filter already logged

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Principal principal = auth != null ? auth : request.getUserPrincipal();
            Long userId = PrincipalUtils.extractUserId(principal);

//            // we don't fetch User from DB here to avoid extra failures; keep user null if unknown
            User userEntity = null;
//
//            ErrorLog ErrorLog = new ErrorLog();
//            ErrorLog.setUser(userEntity);
//            ErrorLog.setEndpoint(request.getRequestURI());
//            ErrorLog.setMethod(request.getMethod());
//            ErrorLog.setStatusCode(statusCode);
//            ErrorLog.setErrorType(ex.getClass().getName());
//            ErrorLog.setErrorMessage(ex.getMessage());
//            ErrorLog.setTracebackInfo(ExceptionUtils.stackTraceAsString(ex));
//
//            errorLogService.save(ErrorLog);
//            request.setAttribute("ERROR_LOGGED", true);
//            log.info("Saved ErrorLog for endpoint {} (userId={})", request.getRequestURI(), userId);
//        } catch (Exception e) {
//            log.error("Failed to persist error log: {}", e.getMessage(), e);
//        }
//    }


            // ---------- Only Short Meaning ----------
            String shortMeaning = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            // Example: "ApiException: User not found"

            ErrorLog errorLog = new ErrorLog();
            errorLog.setUser(userEntity);
            errorLog.setEndpoint(request.getRequestURI());
            errorLog.setMethod(request.getMethod());
            errorLog.setStatusCode(statusCode);
            errorLog.setErrorType(ex.getClass().getSimpleName()); // "ApiException"
            errorLog.setErrorMessage(ex.getMessage());            // "User not found"
            errorLog.setTracebackInfo(shortMeaning);              // "ApiException: User not found"

            errorLogService.save(errorLog);
            request.setAttribute("ERROR_LOGGED", true);

            log.info("Saved ErrorLog for endpoint {} (userId={})", request.getRequestURI(), userId);

        } catch (Exception e) {
            log.error("Failed to persist error log: {}", e.getMessage(), e);
        }
    }

}
