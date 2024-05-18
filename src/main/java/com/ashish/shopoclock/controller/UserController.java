package com.ashish.shopoclock.controller;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


import com.ashish.shopoclock.dto.response.UserResponse;
import com.ashish.shopoclock.model.Avatar;
import com.ashish.shopoclock.model.Role;
import com.ashish.shopoclock.model.User;
import com.ashish.shopoclock.repository.RoleRepository;
import com.ashish.shopoclock.security.JwtUtils;
import com.ashish.shopoclock.service.EmailService;
import com.ashish.shopoclock.service.UserDetailsImpl;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.ashish.shopoclock.dto.request.LoginRequest;
import com.ashish.shopoclock.dto.request.SignupRequest;


@CrossOrigin
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
        String jwt = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

        User user = userService.findById(jwtUtils.getIdFromJwtToken(jwt));

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(jwt);

        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws Exception {
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
        String jwt = userService.loginUser(signUpRequest.getEmail(), signUpRequest.getPassword());

        UserResponse response = new UserResponse();
        response.setUser(user);
        response.setToken(jwt);

        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/logout")
    public ResponseEntity<?> logoutUser() {

        // Setting the security context to null
        SecurityContextHolder.getContext().setAuthentication(null);

        UserResponse response = new UserResponse();
        response.setMessage("Logged Out!");

        return ResponseEntity.ok().body(response);
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
    public ResponseEntity<UserResponse> getUserDetails(HttpServletRequest request) {

        User user = userService.getUserFromJwt(request);

        UserResponse response = new UserResponse();
        response.setUser(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/password/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid UserUpdateRequest userUpdateRequest, HttpServletRequest request) throws Exception {

        User user = userService.getUserFromJwt(request);

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

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PutMapping("/me/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateProfile(@Valid @ModelAttribute UserUpdateRequest userUpdateRequest,  HttpServletRequest request) {

        User user = userService.getUserFromJwt(request);

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