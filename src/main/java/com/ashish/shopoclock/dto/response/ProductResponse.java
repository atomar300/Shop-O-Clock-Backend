package com.ashish.shopoclock.dto.response;

import com.ashish.shopoclock.model.Product;
import com.ashish.shopoclock.model.Review;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    // default value is true
    private boolean success = true;

    private String message;

    private Product product;

    private List<Product> products;

    private Long productsCount;

    private List<Review> reviews;

    private Integer resultPerPage;

    private Long filteredProductsCount;

}
