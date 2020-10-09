package com.ias.ecommerce.repository;

import com.ias.ecommerce.entity.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long> {

}
