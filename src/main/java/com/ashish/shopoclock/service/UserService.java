package com.ashish.shopoclock.service;

import com.ashish.shopoclock.model.Avatar;
import com.ashish.shopoclock.model.ERole;
import com.ashish.shopoclock.model.Role;
import com.ashish.shopoclock.model.User;
import com.ashish.shopoclock.security.JwtUtils;
import com.ashish.shopoclock.exception.UserNotFoundException;
import com.ashish.shopoclock.repository.RoleRepository;
import com.ashish.shopoclock.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

@Service
public class UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CloudinaryImageService cloudinaryImageService;


    public User findByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No User found with the given Email: " + email));

        return user;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }


    public User findByResetPasswordToken(String resetPasswordToken) {
        User user = userRepository.findByResetPasswordToken(resetPasswordToken)
                        .orElseThrow(() -> new BadCredentialsException("Reset Password Token is invalid"));
        return user;
    }

    public User findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No User found with the given ID: " + id));

        return user;
    }


    public void save(User user) {
        userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public String loginUser(String email, String password) throws BadCredentialsException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        return jwt;
    }


    public Avatar processAvatar(String base64) {
        byte[] data = DatatypeConverter.parseBase64Binary(base64.split(",")[1]);
        Map<String, String> imageData = this.cloudinaryImageService.upload(data);
        return new Avatar(imageData.get("public_id"), imageData.get("secure_url"));
    }

    public void deleteAvatar(String public_id){
        this.cloudinaryImageService.delete(public_id);
    }


    public List<Role> mapRoles(List<String> strRoles) {
        return strRoles.stream()
                .map(role -> {
                    switch (role) {
                        case "admin":
                            return roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role 'ADMIN' is not found."));
                        case "mod":
                            return roleRepository.findByName(ERole.ROLE_MODERATOR)
                                    .orElseThrow(() -> new RuntimeException("Error: Role 'MODERATOR' is not found."));
                        case "user":
                            return roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role 'USER' is not found."));
                        default:
                            throw new IllegalArgumentException("Invalid role: " + role);
                    }
                })
                .collect(Collectors.toList());
    }

    public User getUserFromJwt(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        String token ="";
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        } else {
            throw new RuntimeException("Invalid Jwt Token");
        }

        User user = findById(jwtUtils.getIdFromJwtToken(token));
        return user;
    }

}
