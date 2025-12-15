package com.java.sms.service;

import com.java.sms.model.UserStatusLog;

import java.util.List;
import java.util.Optional;

public interface UserStatusLogService {

    List<UserStatusLog> findAll();


    Optional<UserStatusLog> findById(Long id);


    UserStatusLog save(UserStatusLog u);


    void deleteById(Long id);

}
