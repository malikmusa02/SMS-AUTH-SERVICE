package com.java.sms.security;

import com.java.sms.model.User;
import com.java.sms.repository.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new DisabledException("User account is not active. Please contact admin.");
        }

        // Return Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getActive(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                getAuthorities(user)
        );
    }

    // Extract role names into authorities
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())) // ROLE_ prefix
                .collect(Collectors.toSet());
    }
}













































//package com.java.sms.security;
//
//
//import com.java.sms.model.User;
//import com.java.sms.repository.UserRepository;
//import org.hibernate.mapping.Array;
//import org.springframework.security.authentication.DisabledException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserRepository userRepository;
//
//    public CustomUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
//
//
//        if (!user.getActive()) {
//            throw new DisabledException("User account is not active. Please contact admin.");
//        }
//
//        return new CustomUserDetails(user);
//
//
////
////
////
////        return org.springframework.security.core.userdetails.User.builder()
////                .username(user.getEmail())
////                .password(user.getPassword())
////                .roles(roleNames) //  correct type
////                .build();
//
//
//    }
//}
//


