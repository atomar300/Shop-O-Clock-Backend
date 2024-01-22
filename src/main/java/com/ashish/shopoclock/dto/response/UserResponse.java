package com.ashish.shopoclock.dto.response;

import com.ashish.shopoclock.model.User;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserResponse {

    // default value is true
    private boolean success = true;

    private String message;

    private User user;

    private List<User> users;

    private String token;


}
