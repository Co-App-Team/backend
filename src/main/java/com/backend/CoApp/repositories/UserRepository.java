package com.backend.CoApp.repositories;

import com.backend.CoApp.models.UserModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserModel, String> {
    UserModel findUserModelById(String id);
}
