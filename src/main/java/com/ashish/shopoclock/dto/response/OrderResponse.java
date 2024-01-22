package com.ashish.shopoclock.dto.response;

import com.ashish.shopoclock.model.order.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    // default value is true
    private boolean success = true;

    private Order order;

    private List<Order> orders;

    private Double totalAmount;

}
