package com.ashish.shopoclock.dto.request;



import java.util.ArrayList;
import java.util.List;


import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class SignupRequest {

    @NotBlank
    @Size(min = 4, max = 30)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private List<String> roles = new ArrayList<>(List.of("user"));

    @NotBlank
    @Size(min = 8, max = 40)
    private String password;

    private String avatar;

}
