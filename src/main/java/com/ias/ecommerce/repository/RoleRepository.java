package com.ias.ecommerce.repository;

import com.ias.ecommerce.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Integer> {

    public Optional<Role> findByName(String name);

}
