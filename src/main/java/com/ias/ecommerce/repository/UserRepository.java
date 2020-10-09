package com.ias.ecommerce.repository;

import com.ias.ecommerce.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUserName(String userName);

    //@Query("SELECT * FROM User u WHERE u.userName = :userName")
    //Optional<User> findByUsername(@Param("username") String username);
}
