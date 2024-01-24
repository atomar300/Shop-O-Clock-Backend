package com.ashish.shopoclock.controller;


import com.ashish.shopoclock.dto.response.ProductResponse;
import com.ashish.shopoclock.model.Product;
import com.ashish.shopoclock.model.Review;
import com.ashish.shopoclock.model.User;
import com.ashish.shopoclock.service.ProductService;
import com.ashish.shopoclock.service.UserService;
import com.ashish.shopoclock.dto.request.ProductUpdateRequest;
import com.ashish.shopoclock.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;



    @GetMapping("/products")
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(defaultValue = "2147483647", value = "maxPrice", required = false) Integer maxPrice,
            @RequestParam(defaultValue = "1",value = "minPrice", required = false) Integer minPrice,
            @RequestParam(defaultValue = "0", value = "ratings", required = false) Double ratings,
            @RequestParam(defaultValue = "1", value = "page",required = false) Integer page
    ) {
        // Because paging starts from 0 in spring boot but we want it to start at 1.
        page = page - 1;
        int resultPerPage = 8;
        List<Product> products = productService.findAll(keyword, category, maxPrice, minPrice, ratings, page, resultPerPage);

        ProductResponse response = new ProductResponse();
        response.setProducts(products);
        response.setProductsCount(productService.count());
        response.setFilteredProductsCount(productService.filteredProductsCount(keyword, category, maxPrice, minPrice, ratings));
        response.setResultPerPage(resultPerPage);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/product/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable("id") String id) throws ProductNotFoundException {
        ProductResponse response = new ProductResponse();
        response.setProduct(productService.findById(id));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/admin/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> deleteProduct(@PathVariable("id") String id){
        productService.delete(id);
        ProductResponse response = new ProductResponse();
        response.setMessage("Product Deleted Successfully!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/admin/product/new")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody Product product, HttpServletRequest request){

        User user = userService.getUserFromJwt(request);
        product.setUser(user.getId());
        Product _product = productService.save(product);

        ProductResponse response = new ProductResponse();
        response.setProduct(_product);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/admin/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable("id") String id, @Valid @RequestBody ProductUpdateRequest productUpdateRequest) throws ProductNotFoundException {
        ProductResponse response = new ProductResponse();
        response.setProduct(productService.updateProduct(id, productUpdateRequest));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/review")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createProductReview(@RequestBody Map<String, String> payload, HttpServletRequest request) throws ProductNotFoundException {

        if (payload.get("rating") == null){
            throw new IllegalArgumentException("Please Enter the rating!");
        }

        String comment = payload.get("comment") == null ? "" : payload.get("comment");

        User user = userService.getUserFromJwt(request);

        Product product = productService.findById(payload.get("productId"));

        // Checking if reviews already have any review from the current user
        Optional<Review> existingReview = product.getReviews().stream().filter(rev -> rev.getUser().equals(user.getId())).findFirst();
        if (existingReview.isPresent()) {
            existingReview.get().setRating(Double.valueOf(payload.get("rating")));
            existingReview.get().setComment(comment);
        } else {
            Review newReview = new Review(
                    user.getId(),
                    user.getName(),
                    Double.valueOf(payload.get("rating")),
                    comment
            );

            List<Review> reviews = product.getReviews();
            reviews.add(newReview);
            product.setReviews(reviews);
            product.setNumOfReviews(reviews.size());
        }

        double avg = 0d;
        avg = product.getReviews().stream().map(rev -> rev.getRating())
                .reduce(0.0, (a,b) -> a+b);

        product.setRatings(avg/product.getReviews().size());

        productService.save(product);

        ProductResponse response = new ProductResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/reviews")
    public ResponseEntity<ProductResponse> getProductReviews(@RequestParam("id") String id) throws ProductNotFoundException {
//        Product product = productService.findById(id).get();
        Product product = productService.findById(id);
        List<Review> reviews = product.getReviews();
        ProductResponse response = new ProductResponse();
        response.setReviews(reviews);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteReview(@RequestParam("productId") String productId,
                                          @RequestParam("id") String id) throws ProductNotFoundException {
//        Product product = productService.findById(productId).get();
        Product product = productService.findById(productId);

        List<Review> reviews = product.getReviews().stream().filter(rev ->
                !(rev.getId()).equals(id)).toList();

        double avg = 0d;
        avg = reviews.stream().map(rev -> rev.getRating())
                .reduce(0.0, (a,b) -> a+b);

        product.setReviews(reviews);
        product.setRatings(avg/reviews.size());
        product.setNumOfReviews(reviews.size());

        productService.save(product);

        ProductResponse response = new ProductResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
