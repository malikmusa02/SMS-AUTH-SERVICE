package com.java.sms.repository;


import com.java.sms.model.UserStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusLogRepository extends JpaRepository<UserStatusLog, Long> {
}
