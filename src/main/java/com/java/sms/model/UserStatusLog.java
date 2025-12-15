package com.java.sms.model;


import com.java.sms.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.OffsetDateTime;

//This table tracks changes in a user’s account status — like when a user (teacher, student, or staff)
//        is terminated, or reactivated.

@Entity
@Table(name = "UserStatusLog")
@Getter
@Setter
@NoArgsConstructor
public class UserStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // store relation to User (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "status", length = 20)
    private String status; // TERMINATED / REACTIVATED

    @Lob // for lagre object
    @Column(name = "reason")
    private String reason;

    @Column(name = "timestamp")
    private OffsetDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = OffsetDateTime.now();
    }
}
