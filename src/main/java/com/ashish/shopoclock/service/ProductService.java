package com.ashish.shopoclock.service;

import com.ashish.shopoclock.model.Product;
import com.ashish.shopoclock.repository.ProductRepository;
import com.ashish.shopoclock.dto.request.ProductUpdateRequest;
import com.ashish.shopoclock.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private MongoTemplate mongoTemplate;


    public List<Product> findAll(String keyword, String category, Integer maxPrice, Integer minPrice, Double ratings, Integer page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Query dynamicQuery = new Query().with(pageable);

        if (keyword != null) {
            Criteria keywordCriteria = Criteria.where("name").regex(keyword, "i");
            dynamicQuery.addCriteria(keywordCriteria);
        }
        if (category != null){
            Criteria categoryCriteria = Criteria.where("category").is(category);
            dynamicQuery.addCriteria(categoryCriteria);
        }
        if (maxPrice != null && minPrice != null){
            Criteria priceCriteria = Criteria.where("price").gte(minPrice).lte(maxPrice);
            dynamicQuery.addCriteria(priceCriteria);
        }

        if (ratings != null){
            Criteria ratingsCriteria = Criteria.where("ratings").gte(ratings);
            dynamicQuery.addCriteria(ratingsCriteria);
        }

        List<Product> products = mongoTemplate.find(dynamicQuery, Product.class);

        Page<Product> productPage = PageableExecutionUtils.getPage(
                products,
                pageable,
                () -> mongoTemplate.count(dynamicQuery.skip((page-1)*size).limit(size), Product.class)
        );

        return productPage.getContent();
    }


    public long filteredProductsCount(String keyword, String category, Integer maxPrice, Integer minPrice, Double ratings){
        Query dynamicQuery = new Query();

        if (keyword != null) {
            Criteria keywordCriteria = Criteria.where("name").regex(keyword, "i");
            dynamicQuery.addCriteria(keywordCriteria);
        }
        if (category != null){
            Criteria categoryCriteria = Criteria.where("category").is(category);
            dynamicQuery.addCriteria(categoryCriteria);
        }
        if (maxPrice != null && minPrice != null){
            Criteria priceCriteria = Criteria.where("price").gte(minPrice).lte(maxPrice);
            dynamicQuery.addCriteria(priceCriteria);
        }

        if (ratings != null){
            Criteria ratingsCriteria = Criteria.where("ratings").gte(ratings);
            dynamicQuery.addCriteria(ratingsCriteria);
        }

        long count = mongoTemplate.count(dynamicQuery, Product.class);
        return count;
    }


    public long count() {
        return productRepository.count();
    }


    public Product findById(String id) throws ProductNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No Product found for the given ID: " + id));

    }


    public void delete(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No Product found for the given ID: " + id));

        productRepository.delete(product);
    }

    public Product save(Product product){

        // if id is not null or 0 then update otherwise insert
        productRepository.save(product);
        return product;
    }


    public Product updateProduct(String id, ProductUpdateRequest productUpdateRequest) throws ProductNotFoundException {

        Product _product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("No Product found for the given ID: " + id));


        if (productUpdateRequest.getName() != null){
            _product.setName(productUpdateRequest.getName());
        }
        if (productUpdateRequest.getDescription() != null){
            _product.setDescription(productUpdateRequest.getDescription());
        }
        if (productUpdateRequest.getPrice() != null){
            _product.setPrice(productUpdateRequest.getPrice());
        }
        if (productUpdateRequest.getRatings() != null){
            _product.setRatings(productUpdateRequest.getRatings());
        }
        if (productUpdateRequest.getStock() != null){
            _product.setStock(productUpdateRequest.getStock());
        }
        if (productUpdateRequest.getCategory() != null){
            _product.setCategory(productUpdateRequest.getCategory());
        }
        if (productUpdateRequest.getNumOfReviews() != null){
            _product.setNumOfReviews(productUpdateRequest.getNumOfReviews());
        }
        if (productUpdateRequest.getCreatedAt() != null){
            _product.setCreatedAt(productUpdateRequest.getCreatedAt());
        }
        if (!productUpdateRequest.getImages().isEmpty()){
//            imageService.saveAll(productUpdateRequest.getImages());
            _product.setImages(productUpdateRequest.getImages());
        }
        if (!productUpdateRequest.getReviews().isEmpty()){
//            reviewService.saveAll(productUpdateRequest.getReviews());
            _product.setReviews(productUpdateRequest.getReviews());
        }
        if (productUpdateRequest.getUser() != null){
            _product.setUser(productUpdateRequest.getUser());
        }

        productRepository.save(_product);
        return _product;
    }


}

