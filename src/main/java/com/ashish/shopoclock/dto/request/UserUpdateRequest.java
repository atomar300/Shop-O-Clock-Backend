package com.ashish.shopoclock.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateRequest {

    @Size(min = 4, max = 30)
    private String name;

    @Size(max = 50)
    @Email
    private String email;

    private List<String> roles;

    @Size(min = 8, max = 40)
    private String oldPassword;

    @Size(min = 8, max = 40)
    private String newPassword;

    @Size(min = 8, max = 40)
    private String confirmPassword;

    private String avatar;

}
