package com.ashish.shopoclock.dto.request;

import com.ashish.shopoclock.model.Image;
import com.ashish.shopoclock.model.Review;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductUpdateRequest {

    private String name;
    private String description;

    @DecimalMax(value = "99999999.99")
    private Double price;
    private Double ratings;

    @Max(value = 9999)
    private Integer stock;
    private String category;
    private Integer numOfReviews;
    private LocalDateTime createdAt;

    @Valid
    private List<Image> images = new ArrayList<>();

    @Valid
    private List<Review> reviews = new ArrayList<>();
    private String user;
}
