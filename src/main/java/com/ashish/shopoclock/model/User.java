package com.ashish.shopoclock.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank
    @Size(min = 4, max = 30)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @JsonIgnore
    @Size(min = 8, max = 40)
    private String password;

    @Valid
    private Avatar avatar;

    @DBRef
    private List<Role> roles = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    private String resetPasswordToken;

    private LocalDateTime resetPasswordExpire;

    public User(String name, String email, String password, Avatar avatar) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
    }

}