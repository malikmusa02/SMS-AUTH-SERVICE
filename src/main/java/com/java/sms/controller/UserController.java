package com.java.sms.controller;

import com.java.sms.DataClass.UserDTO;
import com.java.sms.model.User;
import com.java.sms.repository.UserRepository;
import com.java.sms.response.UserResponse;
import com.java.sms.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/users")
public class UserController {


    private final UserService userService;
    private final UserRepository userRepository;


    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {

        this.userService = userService;

        this.userRepository = userRepository;
    }
 



    @GetMapping("/getAllUser")
    public List<UserResponse> getAllUser(){

        return userService.getAllUser();


    }


    @GetMapping("/getById/{id}")
    public UserResponse getById(@PathVariable Long id){
        return userService.getById(id);
    }



    @GetMapping("/getUserImage/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId){
        return userService.getUserImage(userId);
    }

    @PostMapping(value = "/upload-image/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadUserImage(
            @PathVariable Long userId,
            @ModelAttribute MultipartFile image
    ) {
        return userService.uploadUserImage(userId, image);
    }


    @PutMapping("/update-image/{userId}")
    public ResponseEntity<?> updateImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile file) {
        return userService.updateUserImage(userId, file);
    }




//    //  One POST: Create User + Upload Image
//    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
//    public ResponseEntity<?> createUser(@ModelAttribute UserDTO request) throws IOException {
//        return userService.createUser(request);
//    }

    @PostMapping("/create")
    public ResponseEntity<?> createUsers(@Valid @RequestBody UserDTO userDTO){
        System.out.println(userDTO);
        return userService.createUser(userDTO);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO request){
        return userService.updateUser(id,request);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id){
        return userService.deleteUser(id);
    }


}
