package com.java.sms.serviceImpl;

import com.java.sms.model.UserStatusLog;
import com.java.sms.repository.UserStatusLogRepository;
import com.java.sms.service.UserStatusLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserStatusLogServiceImpl implements UserStatusLogService {


    private final UserStatusLogRepository repository;



    public UserStatusLogServiceImpl(UserStatusLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UserStatusLog> findAll() { return repository.findAll(); }



    @Override
    public Optional<UserStatusLog> findById(Long id) { return repository.findById(id); }


    @Override
    public UserStatusLog save(UserStatusLog u) { return repository.save(u); }



    @Override
    public void deleteById(Long id) { repository.deleteById(id); }
}
