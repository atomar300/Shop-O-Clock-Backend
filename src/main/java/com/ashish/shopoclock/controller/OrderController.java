package com.ashish.shopoclock.controller;


import com.ashish.shopoclock.dto.response.OrderResponse;
import com.ashish.shopoclock.model.Product;
import com.ashish.shopoclock.model.User;
import com.ashish.shopoclock.service.OrderService;
import com.ashish.shopoclock.service.ProductService;
import com.ashish.shopoclock.service.UserService;
import com.ashish.shopoclock.model.order.Order;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@CrossOrigin
@RestController
@RequestMapping("/api/v1")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;


    @PreAuthorize("hasRole('USER')")
    @PostMapping("/order/new")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody Order order, HttpServletRequest request){
        User user = userService.getUserFromJwt(request);
        order.setUser(user.getId());
        order.setPaidAt(LocalDateTime.now());


        /*****************************************************/
        // Remove this code within /* to */ once the admin functionality is done
        order.getOrderItems().stream().forEach(o -> {
            Product product = productService.findById(o.getProduct());
            product.setStock(product.getStock() - o.getQuantity());
            productService.save(product);
        });
        /*****************************************************/


        orderService.save(order);

        OrderResponse response = new OrderResponse();
        response.setOrder(order);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/order/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSingleOrder(@PathVariable("id") String id){
        Order order = orderService.findById(id);
        User user = userService.findById(order.getUser());

        // LinkedHashMap so that order of insertion is maintained
        Map<String, String> userDetails = new LinkedHashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("name", user.getName());
        userDetails.put("email", user.getEmail());

        // LinkedHashMap so that order of insertion is maintained
        Map<String, Object> orderDetails = new LinkedHashMap<>();
        orderDetails.put("id", order.getId());
        orderDetails.put("shippingInfo", order.getShippingInfo());
        orderDetails.put("orderItems", order.getOrderItems());
        orderDetails.put("user", userDetails);
        orderDetails.put("paymentInfo", order.getPaymentInfo());
        orderDetails.put("paidAt", order.getPaidAt());
        orderDetails.put("itemsPrice", order.getItemsPrice());
        orderDetails.put("taxPrice", order.getTaxPrice());
        orderDetails.put("shippingPrice", order.getShippingPrice());
        orderDetails.put("totalPrice", order.getTotalPrice());
        orderDetails.put("orderStatus", order.getOrderStatus());
        orderDetails.put("createdAt", order.getCreatedAt());


        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("order", orderDetails);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PreAuthorize("hasRole('USER')")
    @GetMapping("/orders/me")
    public ResponseEntity<OrderResponse> myOrders(HttpServletRequest request ){
        User user = userService.getUserFromJwt(request);
        List<Order> orders = orderService.findByUser(user.getId());

        OrderResponse response = new OrderResponse();
        response.setOrders(orders);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public ResponseEntity<?> getAllOrders(){
        List<Order> orders = orderService.findAll();

        double totalAmount = 0d;
        totalAmount = orders.stream().map(ord -> ord.getTotalPrice()).reduce(0d, (a,b) -> a+b);

        OrderResponse response = new OrderResponse();
        response.setOrders(orders);
        response.setTotalAmount(totalAmount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/order/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable("id") String id,
                                         @RequestBody Map<String, String> payload){
        Order order = orderService.findById(id);
        if (order.getOrderStatus().equals("Delivered")){
            throw new RuntimeException("You have already delivered this order");
        }

        if (payload.get("status").equals("Shipped")){
            order.getOrderItems().stream().forEach(o -> {
//                Product product = productService.findById(o.getProduct()).get();
                Product product = productService.findById(o.getProduct());
                product.setStock(product.getStock() - o.getQuantity());
                productService.save(product);
            });
        }

        order.setOrderStatus(payload.get("status"));

        if (payload.get("status").equals("Delivered")){
            order.setDeliveredAt(LocalDateTime.now());
        }

        orderService.save(order);

        OrderResponse response = new OrderResponse();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/order/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") String id){
        orderService.delete(id);

        OrderResponse response = new OrderResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
