package com.ashish.shopoclock.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
@Data // this annotation takes care of all the getters and setters and toString() method.
public class Product {

    @Id
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMax(value = "99999999.99")
    private Double price;

    private Double ratings = 0d;

    @Max(value = 9999)
    private Integer stock = 1;

    @NotBlank
    private String category;

    private Integer numOfReviews = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Valid
    private List<Image> images = new ArrayList<>();

    @Valid
    private List<Review> reviews = new ArrayList<>();

    private String user;

    public Product(String name, String description, Double price, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }
}

