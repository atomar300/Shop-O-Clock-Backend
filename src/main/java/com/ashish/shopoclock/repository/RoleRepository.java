package com.ashish.shopoclock.repository;

import java.util.Optional;

import com.ashish.shopoclock.model.ERole;
import com.ashish.shopoclock.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(ERole name);
}
