// package com.java.sms.controller;

// import com.java.sms.DataClass.DirectorDTO;
// import com.java.sms.model.Role;
// import com.java.sms.model.User;
// import com.java.sms.openFeignClient.DirectorClient;
// import com.java.sms.repository.RoleRepository;
// import com.java.sms.repository.UserRepository;
// import com.java.sms.response.DirectorsResponse;
// import jakarta.transaction.Transactional;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Component;

// import java.util.HashSet;
// import java.util.List;
// import java.util.Scanner;
// import java.util.Set;

// @Component
// public class SuperUserCreator implements CommandLineRunner {

//     private final UserRepository userRepository;
//     private final RoleRepository roleRepository;
//     private final PasswordEncoder passwordEncoder;
//     private final DirectorClient directorClient;

//     public SuperUserCreator(UserRepository userRepository,
//                             RoleRepository roleRepository,
//                             PasswordEncoder passwordEncoder,
//                             DirectorClient directorClient) {
//         this.userRepository = userRepository;
//         this.roleRepository = roleRepository;
//         this.passwordEncoder = passwordEncoder;
//         this.directorClient = directorClient;
//     }

//     @Override
//     @Transactional
//     public void run(String... args) {

//         //  Check if Director already exists in Django


        

//          try {
//             DirectorsResponse resp; = directorClient.getAllDirectors();
//             List<DirectorDTO> existingDirectors = resp.getDirectors();

//              System.out.println("\n\n\n\n\n\n" + "Object of director : "+ existingDirectors);
//             if (!existingDirectors.isEmpty()) {
//             System.out.println("Director already exists in Django, skipping superuser creation.");
//             return;
//         }

//         } catch (FeignException.Unauthorized e) {
//             log.warn("Director service unauthorized, skipping startup sync");
//         } catch (Exception e) {
//             log.error("Startup sync failed", e);
//         }

        

//         Scanner scanner = new Scanner(System.in);
//         System.out.println("=== Create Superuser Director ===");

//         // Required fields
//         System.out.print("First Name (required): ");
//         String firstName = scanner.nextLine();
//         if (firstName == null || firstName.isBlank()) {
//             System.out.println(" First name is required!");
//             return;
//         }

//         System.out.print("Email (required): ");
//         String email = scanner.nextLine();
//         if (email == null || email.isBlank()) {
//             System.out.println(" Email is required!");
//             return;
//         }

//         System.out.print("Password (required): ");
//         String password = scanner.nextLine();
//         if (password == null || password.isBlank()) {
//             System.out.println(" Password is required!");
//             return;
//         }

//         // Optional fields
//         System.out.print("Middle Name (optional): ");
//         String middleName = scanner.nextLine();

//         System.out.print("Last Name (optional): ");
//         String lastName = scanner.nextLine();

//         System.out.print("Image Path (optional): ");
//         String imagePath = scanner.nextLine();

//         System.out.print("Phone No (optional): ");
//         String phoneNo = scanner.nextLine();

//         System.out.print("Qualification (optional): ");
//         String qualification = scanner.nextLine();

//         System.out.print("Gender (optional): ");
//         String gender = scanner.nextLine();

//         System.out.print("Aadhar No (optional): ");
//         String aadharNo = scanner.nextLine();

//         System.out.print("PAN No (optional): ");
//         String panNo = scanner.nextLine();

//         //  Fetch local DIRECTOR role (must exist in Role table)
//         Role directorRole = roleRepository.findByName("director")
//                 .orElseThrow(() -> new RuntimeException("Role not found!"));

//         Set<Role> userRoles = new HashSet<>();
//         userRoles.add(directorRole);


//         //  Create User with all fields
//         User user = new User();
//         user.setFirstName(firstName);
//         user.setMiddleName(middleName.isBlank() ? null : middleName);
//         user.setLastName(lastName.isBlank() ? null : lastName);
//         user.setEmail(email);
//         user.setPassword(passwordEncoder.encode(password));
//         user.setImagePath(imagePath.isBlank() ? null : imagePath);
//         user.setPhoneNo(phoneNo.isBlank() ? null : phoneNo);
//         user.setQualification(qualification.isBlank() ? null : qualification);
//         user.setGender(gender.isBlank() ? null : gender);
//         user.setAadharNo(aadharNo.isBlank() ? null : aadharNo);
//         user.setPanNo(panNo.isBlank() ? null : panNo);
//         user.setRoles(userRoles);

//         User save = userRepository.save(user);


//         // Create linked Director in Django
//         DirectorDTO directorDTO = new DirectorDTO();
//         directorDTO.setUser(user.getId()); // send Spring user_id to Django

//         DirectorDTO createdDirector = directorClient.createDirector(directorDTO);

//         //  Confirmation message
//         System.out.println("\n Superuser Director created successfully!");
// //        System.out.println("Spring User ID: " + user.getId());
//         System.out.println("Django Director ID: " + createdDirector.getId());
//     }
// }
