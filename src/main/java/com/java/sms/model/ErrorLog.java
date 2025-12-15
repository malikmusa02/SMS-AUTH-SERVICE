package com.java.sms.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;


/**
 * Represents any error that occurs in the system,
 * Captures request info, user, error type, message, and full stacktrace.
 */

@Entity
@Table(name = "ErrorLog")
@Getter
@Setter
@NoArgsConstructor
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // optional reference to application User (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "endpoint", length = 1024)
    private String endpoint;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_type", length = 255)
    private String errorType;

    @Lob // for large object
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Lob
    @Column(name = "traceback_info",columnDefinition = "TEXT")
    private String tracebackInfo;

    @Column(name = "timestamp")
    private OffsetDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = OffsetDateTime.now();
    }
}
