package com.ias.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order implements Serializable {

    public enum OrderStatus{
        REGISTRADA,
        PAGADA,
        ENTREGADA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private float total;
    private LocalDateTime dateOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FK_USER", nullable = false)
    @JsonIgnore
    private User client;

    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItemList;

    public Order(){}

    public Order(long total, LocalDateTime dateOrder, OrderStatus orderStatus){
        this.total = total;
        this.dateOrder = dateOrder;
        this.orderStatus = orderStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public LocalDateTime getDateOrder() {
        return dateOrder;
    }

    public void setDateOrder(LocalDateTime dateOrder) {
        this.dateOrder = dateOrder;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
