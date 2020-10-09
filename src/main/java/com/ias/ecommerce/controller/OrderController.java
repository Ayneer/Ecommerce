package com.ias.ecommerce.controller;

import com.ias.ecommerce.Service.ProductService;
import com.ias.ecommerce.Service.UserService;
import com.ias.ecommerce.entity.Order;
import com.ias.ecommerce.entity.OrderItem;
import com.ias.ecommerce.entity.Product;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.OrderRepository;
import com.ias.ecommerce.repository.ProductRepository;
import com.ias.ecommerce.repository.UserRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class OrderController {

    private OrderRepository orderRepository;
    private final UserService userService;
    private final ProductService productService;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository){
        this.orderRepository = orderRepository;
        this.userService = new UserService(userRepository);
        this.productService = new ProductService(productRepository);
    }

    @PostMapping("/order")
    public ResponseEntity<Object> create(@RequestBody List<OrderItemJson> orderItemJsonList){

        String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        User user = userService.findByUsername(username).orElseThrow(() -> new DataNotFoundException("The User : "+username+" do not exist."));

        List<OrderItem> orderItemList = getOrderItems(orderItemJsonList);

        Order newOrder = new Order();
        newOrder.setClient(user);
        newOrder.setDateOrder(LocalDateTime.now());

        orderItemList.forEach( orderItem -> orderItem.setOrder(newOrder) );

        newOrder.setOrderItemList(orderItemList);
        newOrder.setTotal(0);

        orderRepository.save(newOrder);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Order created successfully", newOrder, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private List<OrderItem> getOrderItems(List<OrderItemJson> orderItemJsonList){

        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderItemJson orderItemJsonListItem: orderItemJsonList) {

            if(orderItemJsonListItem.quantity <= 0){
                throw new OperationNotCompletedException("You have to select greater than 0 product quantity");
            }

            Product product = productService.findById(orderItemJsonListItem.productId);

            if(product.getInventoryQuantity() < orderItemJsonListItem.quantity){
                throw new OperationNotCompletedException("The inventory quantity of "+product.getName()+" is insufficient. Actual: "+product.getInventoryQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(orderItemJsonListItem.quantity);
            orderItem.setConfirmed(false);
            orderItem.setProduct(product);

            orderItemList.add(orderItem);
        }

        return orderItemList;
    }

}

class OrderItemJson{

    Integer quantity;
    long productId;

    public OrderItemJson(Integer quantity, long productId) {
        this.quantity = quantity;
        this.productId = productId;
    }
}
