package com.ashish.shopoclock.model.order;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Valid
    private ShippingInfo shippingInfo;

    @Valid
    private List<OrderItems> orderItems;

    private String user;

    @Valid
    private PaymentInfo paymentInfo;

    private LocalDateTime paidAt;

    private Double itemsPrice = 0d;

    private Double taxPrice = 0d;

    private Double shippingPrice = 0d;

    private Double totalPrice = 0d;

    private String orderStatus = "Processing";

    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt = LocalDateTime.now();

}
