//package com.java.sms.mapper;
//
//
//import com.java.sms.DataClass.UserDTO;
//import com.java.sms.model.user.Role;
//import com.java.sms.model.User;
//import com.java.sms.repository.RoleRepository;
//import com.java.sms.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.Set;
//
//@Component
//@RequiredArgsConstructor
//public class UserMapper {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//
//    public User createUser(UserDTO userRequest){
//
//        User user = new User();
//
//        user.setEmail(userRequest.getEmail());
//        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
//        user.setFirstName(userRequest.getFirstName());
//        user.setMiddleName(userRequest.getMiddleName());
//        user.setImagePath( "C://Users/Hp//OneDrive//Pictures//Saved Pictures/");
//        user.setLastName(userRequest.getLastName());
//        Role role = roleRepository.findById(userRequest.getRoleId())
//                .orElseThrow(() -> new RuntimeException("role id not found"));
//
////        Role role = roleRepository.findByName().orElseThrow(() -> new RuntimeException("role not found in the data base"));
//
//        user.setRoles(Set.of(role));
//
//        return userRepository.save(user);
//    }
//}
