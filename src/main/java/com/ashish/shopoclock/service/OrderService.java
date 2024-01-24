package com.ashish.shopoclock.service;

import com.ashish.shopoclock.repository.OrderRepository;
import com.ashish.shopoclock.exception.OrderNotFoundException;
import com.ashish.shopoclock.model.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public void save(Order order){
        orderRepository.save(order);
    }

    public Order findById(String id){

        Order order = orderRepository.findById(id)
                        .orElseThrow(() -> new OrderNotFoundException("No order found with the given ID: " + id));
        return order;
    }

    public void delete(String id){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("No order found with the given ID: " + id));

        orderRepository.delete(order);
    }

    public List<Order> findByUser(String id){
        return orderRepository.findByUser(id);
    }

    public List<Order> findAll(){
        return orderRepository.findAll();
    }
}
