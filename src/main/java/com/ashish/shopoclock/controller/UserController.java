package com.ashish.shopoclock.controller;


import java.time.LocalDateTime;
import java.util.*;


import com.ashish.shopoclock.dto.response.UserResponse;
import com.ashish.shopoclock.model.Avatar;
import com.ashish.shopoclock.model.Role;
import com.ashish.shopoclock.model.User;
import com.ashish.shopoclock.repository.RoleRepository;
import com.ashish.shopoclock.security.JwtUtils;
import com.ashish.shopoclock.service.EmailService;
import com.ashish.shopoclock.service.UserService;
import com.ashish.shopoclock.exception.UserNotFoundException;
import com.ashish.shopoclock.dto.request.UserUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ashish.shopoclock.dto.request.LoginRequest;
import com.ashish.shopoclock.dto.request.SignupRequest;


@CrossOrigin(origins = "*", maxAge = 3600)
@EnableAsync
@RestController
@RequestMapping("/api/v1")
public class UserController {


    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final EmailService emailService;


    @Autowired
    public UserController(RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils, UserService userService, EmailService emailService) {
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.emailService = emailService;
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) throws BadCredentialsException {
        ResponseCookie jwtCookie = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        User user = userService.getUserFromCookie(jwtCookie.getValue());

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(jwtCookie.getValue().toString());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @ModelAttribute SignupRequest signUpRequest) throws Exception {
        if (userService.existsByEmail(signUpRequest.getEmail())) {
            throw new Exception("Email is already taken!");
        }

        String base64 = signUpRequest.getAvatar();
        Avatar avatar = userService.processAvatar(base64);

        // Create new user's account
        User user = new User(signUpRequest.getName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                avatar
        );

        List<String> strRoles = signUpRequest.getRoles();
        List<Role> roles = userService.mapRoles(strRoles);

        user.setRoles(roles);
        userService.save(user);

        // Sending jwt token without having to login
        ResponseCookie jwtCookie = userService.loginUser(signUpRequest.getEmail(), signUpRequest.getPassword());

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(jwtCookie.getValue().toString());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }


//    Previous registerUser code
//    @PostMapping("/register")
//    public ResponseEntity<?> registerUser(@Valid @ModelAttribute SignupRequest signUpRequest) throws Exception {
//        if (userService.existsByEmail(signUpRequest.getEmail())) {
//            throw new Exception("Email is already taken!");
//        }
//
//        String base64 = signUpRequest.getAvatar();
//        byte[] data = DatatypeConverter.parseBase64Binary(base64.split(",")[1]);
//        Map imageData = this.cloudinaryImageService.upload(data);
//        Avatar avatar = new Avatar((String) imageData.get("public_id"), (String) imageData.get("secure_url"));
//
//
//        // Create new user's account
//        User user = new User(signUpRequest.getName(),
//                signUpRequest.getEmail(),
//                encoder.encode(signUpRequest.getPassword()),
//                avatar
//                );
//
//        List<String> strRoles = signUpRequest.getRoles();
//        List<Role> roles = new ArrayList<>();
//
//        strRoles.forEach(role -> {
//            switch (role) {
//                case "admin":
//                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(adminRole);
//
//                    break;
//                case "mod":
//                    Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(modRole);
//
//                    break;
//                case "user":
//                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(userRole);
//            }
//        });
//
//        user.setRoles(roles);
//        userService.save(user);
//
//        // Sending jwt token without having to login
//        ResponseCookie jwtCookie = userService.loginUser(signUpRequest.getEmail(), signUpRequest.getPassword());
//
//        UserResponse response = new UserResponse();
//        response.setUser(user);
//        response.setToken(jwtCookie.getValue().toString());
//        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
//                .body(response);
//    }


    @GetMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Setting the security context to null
        SecurityContextHolder.getContext().setAuthentication(null);

        // Setting the value of cookie to empty String
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        UserResponse response = new UserResponse();
        response.setMessage("Logged Out!");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload, HttpServletRequest request) throws UserNotFoundException {

        // Lookup user in database by e-mail
        User user = userService.findByEmail(payload.get("email"));

        // Generate random 36-character string token for reset password
        String resetToken = UUID.randomUUID().toString();
        user.setResetPasswordToken(DigestUtils.sha256Hex(resetToken));
        user.setResetPasswordExpire(LocalDateTime.now().plusMinutes(15));

        // Save token to database
        userService.save(user);

        //String appUrl = request.getScheme() + "://" + request.getServerName();
        //String resetPasswordUrl = appUrl + "/api/v1/password/reset/" + resetToken;
        String appUrl = "http://localhost:3000"; //ReactJs would be running on port 3000
        String resetPasswordUrl = appUrl + "/password/reset/" + resetToken;

        String to = user.getEmail();
        String subject = "Shop O'Clock Password Recovery";
        String message = "Your password reset token is:\n\n" + resetPasswordUrl
                + "\n\n If you have not requested this email then, please ignore it.";

        emailService.sendSimpleMail(to, subject, message);

        UserResponse response = new UserResponse();
        response.setMessage("Email sent to " + payload.get("email") + " successfully!");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/password/reset/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable("token") String token,
                                           @RequestBody @Valid UserUpdateRequest userUpdateRequest) {

        String resetPasswordToken = DigestUtils.sha256Hex(token);

        User user = userService.findByResetPasswordToken(resetPasswordToken);

        if (!(user.getResetPasswordExpire().isAfter(LocalDateTime.now()))) {
            throw new BadCredentialsException("Reset Password Token is expired");
        }

        if (!userUpdateRequest.getNewPassword().equals(userUpdateRequest.getConfirmPassword())) {
            throw new BadCredentialsException("Password do not match");
        }

        user.setPassword(encoder.encode(userUpdateRequest.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpire(null);

        userService.save(user);

        UserResponse response = new UserResponse();
        response.setUser(user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> getUserDetails(@CookieValue("ashish") String ashishCookie) {
        // Cookie name is ashish and value of this cookie is the JwtToken. Cookie has many different attributes like value, maxtime, path etc.
        User user = userService.getUserFromCookie(ashishCookie);

        UserResponse response = new UserResponse();
        response.setUser(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/password/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid UserUpdateRequest userUpdateRequest, @CookieValue("ashish") String ashishCookie) throws Exception {

        // Cookie name is ashish and value of this cookie is the JwtToken. i.e. ashish = value of token
        User user = userService.getUserFromCookie(ashishCookie);

        // Encoder is using bCryptPasswordEncoder in the background.
        // Comparing the passwords using matches() method in bCryptPasswordEncoder.
        boolean isPasswordMatched = encoder.matches(userUpdateRequest.getOldPassword(), user.getPassword());

        if (!isPasswordMatched) {
            throw new Exception("Old password is incorrect!");
        }

        if (!userUpdateRequest.getNewPassword().equals(userUpdateRequest.getConfirmPassword())) {
            throw new Exception("Password do not match");
        }

        user.setPassword(encoder.encode(userUpdateRequest.getNewPassword()));
        userService.save(user);

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(ashishCookie);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/me/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute UserUpdateRequest userUpdateRequest, @CookieValue("ashish") String ashishCookie) {

        // Cookie name is ashish and value of this cookie is the JwtToken. i.e. ashish = value of token
        User user = userService.getUserFromCookie(ashishCookie);

        if (!userUpdateRequest.getAvatar().equals("")) {
            String imageId = user.getAvatar().getPublic_id();
            userService.deleteAvatar(imageId);

            String base64 = userUpdateRequest.getAvatar();
            Avatar avatar = userService.processAvatar(base64);

            user.setAvatar(avatar);
        }

        user.setName(userUpdateRequest.getName());
        user.setEmail(userUpdateRequest.getEmail());
        userService.save(user);

        UserResponse response = new UserResponse();

        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getAllUsers() {
        List<User> users = userService.findAll();

        UserResponse response = new UserResponse();
        response.setUsers(users);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getSingleUser(@PathVariable("id") String id) {
        User user = userService.findById(id);

        UserResponse response = new UserResponse();
        response.setUser(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@Valid @RequestBody UserUpdateRequest userUpdateRequest, @PathVariable("id") String id) {

        User user = userService.findById(id);

        List<String> strRoles = userUpdateRequest.getRoles();

//        strRoles.forEach(role -> {
//            switch (role) {
//                case "admin":
//                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(adminRole);
//
//                    break;
//                case "mod":
//                    Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(modRole);
//
//                    break;
//                default:
//                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    roles.add(userRole);
//            }
//        });


        // We may not want to change name so for that we wrote a condition
        if (userUpdateRequest.getName() != null) {
            user.setName(userUpdateRequest.getName());
        }

        // We may not want to change email so for that we wrote a condition
        if (userUpdateRequest.getEmail() != null) {
            user.setEmail(userUpdateRequest.getEmail());
        }

        List<Role> roles = userService.mapRoles(strRoles);

        user.setRoles(roles);
        userService.save(user);

        UserResponse response = new UserResponse();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/admin/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable("id") String id) throws UserNotFoundException {
        User user = userService.findById(id);
        userService.delete(user);

        UserResponse response = new UserResponse();
        response.setMessage("User Deleted Successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}