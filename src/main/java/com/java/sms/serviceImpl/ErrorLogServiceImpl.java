package com.java.sms.serviceImpl;



import com.java.sms.exception.ApiException;
import com.java.sms.model.ErrorLog;
import com.java.sms.model.User;
import com.java.sms.repository.ErrorLogRepository;
import com.java.sms.repository.UserRepository;
import com.java.sms.service.ErrorLogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Service responsible for persisting ErrorLog entries.
 * Used by both GlobalErrorLoggingFilter and GlobalExceptionHandler.
 */
@Service
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogRepository errorLogRepository;
    private final UserRepository userRepository;


    public ErrorLogServiceImpl(ErrorLogRepository errorLogRepository, UserRepository userRepository) {
        this.errorLogRepository = errorLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a prepared ErrorLog entry.
     */


    public ErrorLog save(ErrorLog log) {
        return errorLogRepository.save(log);
    }

//    /**
//     * Convenience method to build and save a new ErrorLog record in one step.
//     */
//    public ErrorLog createAndSave(Long userId,
//                                  String endpoint,
//                                  String method,
//                                  int statusCode,
//                                  String errorType,
//                                  String errorMessage,
//                                  String traceback) {
//
//        User userIdNotFound = userRepository.findById(userId)
//                .orElseThrow(() -> new ApiException("User id not found", HttpStatus.NOT_FOUND));
//
//        ErrorLog log = new ErrorLog();
//        log.setUser(userIdNotFound);
//        log.setEndpoint(endpoint);
//        log.setMethod(method);
//        log.setStatusCode(statusCode);
//        log.setErrorType(errorType);
//        log.setErrorMessage(errorMessage);
//        log.setTracebackInfo(traceback);
//
//        return errorLogRepository.save(log);
//    }
}
