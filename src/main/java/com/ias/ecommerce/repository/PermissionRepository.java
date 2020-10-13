package com.ias.ecommerce.repository;

import com.ias.ecommerce.entity.Permission;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PermissionRepository extends CrudRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

}
