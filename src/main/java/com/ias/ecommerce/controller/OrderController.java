package com.ias.ecommerce.controller;

import com.ias.ecommerce.service.ProductService;
import com.ias.ecommerce.service.UserService;
import com.ias.ecommerce.entity.Order;
import com.ias.ecommerce.entity.OrderItem;
import com.ias.ecommerce.entity.Product;
import com.ias.ecommerce.entity.User;
import com.ias.ecommerce.exception.ApiResponse;
import com.ias.ecommerce.exception.customs.AuthorizationException;
import com.ias.ecommerce.exception.customs.DataNotFoundException;
import com.ias.ecommerce.exception.customs.OperationNotCompletedException;
import com.ias.ecommerce.repository.OrderRepository;
import com.ias.ecommerce.repository.ProductRepository;
import com.ias.ecommerce.repository.UserRepository;
import com.ias.ecommerce.security.Auth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private static final double PERCENT_DISCOUNT = 0.1;
    private OrderRepository orderRepository;
    private final ProductService productService;
    private final Auth auth;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository, ProductRepository productRepository){
        this.orderRepository = orderRepository;
        this.productService = new ProductService(productRepository);
        this.auth = new Auth(new UserService(userRepository));
    }

    @PostMapping("/order")
    public ResponseEntity<Object> create(@RequestBody List<OrderItemJson> orderItemJsonList){

        User user = auth.getUserAuth();

        List<OrderItem> orderItemList = getOrderItems(orderItemJsonList);

        Order newOrder = new Order();
        newOrder.setClient(user);
        newOrder.setDateOrder(LocalDateTime.now());

        orderItemList.forEach( orderItem -> orderItem.setOrder(newOrder) );

        newOrder.setOrderItemList(orderItemList);
        newOrder.setTotal(getOrderTotal(orderItemList));
        newOrder.setOrderStatus(Order.OrderStatus.REGISTRADA);

        orderRepository.save(newOrder);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Order created successfully", newOrder, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/orders")
    public ResponseEntity<Object> getOrders(){

        User user = auth.getUserAuth();

        if(auth.isCostumer()){

            ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Orders read successfully", user.getOrderList(), HttpStatus.OK.value(), true);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        }else if(auth.isEmployee()){

            List<Order> orderList = new ArrayList<>();
            orderRepository.findAll().forEach(order -> {
                if(order.getOrderStatus().equals(Order.OrderStatus.PAGADA)){
                    boolean isMatch = order.getOrderItemList().stream().anyMatch( orderItem -> orderItem.getProduct().getUser().getId().equals(user.getId()));
                    if(isMatch){
                        orderList.add(order);
                    }
                }
            });

            ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Orders read successfully", orderList, HttpStatus.OK.value(), true);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);

        }else{
            throw new AuthorizationException("You can not do this action!");
        }
    }

    @PostMapping("/order/pay/{id}")
    public ResponseEntity<Object> paidOrder(@PathVariable(value = "id") long id){
        User user = auth.getUserAuth();

        Order order = orderRepository.findById(id).orElseThrow( () -> new DataNotFoundException("Do not exist the order with ID: "+id) );

        if(!user.getId().equals(order.getClient().getId())){
            throw new AuthorizationException("You can not do this action!");
        }

        order.getOrderItemList().forEach(orderItem -> {
            if(orderItem.getProduct().getInventoryQuantity() < orderItem.getQuantity()){
                throw new OperationNotCompletedException("The inventory quantity of "+orderItem.getProduct().getName()+" is insufficient. Actual: "+orderItem.getProduct().getInventoryQuantity());
            }
        });

        order.getOrderItemList().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            product.setInventoryQuantity( orderItem.getProduct().getInventoryQuantity() - orderItem.getQuantity() );
            productService.update(product);
        });

        order.setOrderStatus(Order.OrderStatus.PAGADA);

        boolean userHasDiscount = hasDiscount(user.getOrderList());

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("datePaid", LocalDateTime.now());
        ticket.put("clientName", order.getClient().getUserDetail().getName());
        ticket.put("subTotal", order.getTotal());
        ticket.put("discount", userHasDiscount ? "10%" : "0%");
        ticket.put("Total", userHasDiscount ? (order.getTotal() - (order.getTotal()*PERCENT_DISCOUNT)) : order.getTotal() );

        orderRepository.save(order);

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Order paid successfully", ticket, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<Object> orderDetail(@PathVariable(value = "id") long id){

        User user = auth.getUserAuth();

        Order order = orderRepository.findById(id).orElseThrow( () -> new DataNotFoundException("Do not exist the order with ID: "+id) );

        if(!order.getClient().getId().equals(user.getId())){
            throw new AuthorizationException("You can not read this resource");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("Order", order);
        data.put("Products", getProductsDetails(order.getOrderItemList()));

        ApiResponse apiResponse = new ApiResponse(HttpStatus.OK, "Order details read successfully", data, HttpStatus.OK.value(), true);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private boolean hasDiscount(List<Order> orderList){
        long countPaidOrders = orderList.stream().filter( order -> order.getOrderStatus().equals(Order.OrderStatus.PAGADA) ).count();
        return (countPaidOrders % 3) == 0;
    }

    private List<Map<String, Object>> getProductsDetails(List<OrderItem> orderItemList){

        Map<String, Object> productDetails = new HashMap<>();
        List<Map<String, Object>> productDetailList = new ArrayList<>();
        orderItemList.forEach( orderItem -> {
            productDetails.put("productId", orderItem.getProduct().getProductId());
            productDetails.put("name", orderItem.getProduct().getName());
            productDetails.put("description", orderItem.getProduct().getDescription());
            productDetails.put("price", (orderItem.getProduct().getTaxRate() * orderItem.getProduct().getBasePrice()) + orderItem.getProduct().getBasePrice());
            productDetails.put("quantity", orderItem.getQuantity());
            productDetailList.add(productDetails);
        } );

        return productDetailList;
    }

    private float getOrderTotal(List<OrderItem> orderItemList){
        float total = 0;
        for (OrderItem orderItem : orderItemList ){
            float productPrice = (orderItem.getProduct().getTaxRate() * orderItem.getProduct().getBasePrice()) + orderItem.getProduct().getBasePrice();
            total += productPrice * orderItem.getQuantity();
        }
        return total;
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
