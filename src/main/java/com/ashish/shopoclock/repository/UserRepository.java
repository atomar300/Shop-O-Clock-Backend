package com.ashish.shopoclock.repository;

import java.util.Optional;

import com.ashish.shopoclock.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

}
