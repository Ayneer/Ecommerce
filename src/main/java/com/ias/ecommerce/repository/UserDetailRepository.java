package com.ias.ecommerce.repository;

import com.ias.ecommerce.entity.UserDetail;
import org.springframework.data.repository.CrudRepository;

public interface UserDetailRepository extends CrudRepository<UserDetail, String> {
}
