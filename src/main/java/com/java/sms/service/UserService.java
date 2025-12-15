package com.java.sms.service;


import com.java.sms.DataClass.UserDTO;
import com.java.sms.response.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.Map;



public interface UserService {

    List<UserResponse> getAllUser();

    ResponseEntity<?> createUser(UserDTO request);

    public ResponseEntity<?> getUserImage(Long id);

    public ResponseEntity<?> uploadUserImage(Long userId, MultipartFile image);

    ResponseEntity<?> updateUser(Long id,UserDTO request);

    ResponseEntity<String> deleteUser(Long id);


    UserResponse getById(Long id);

    ResponseEntity<?> updateUserImage(Long id, MultipartFile file);

}

