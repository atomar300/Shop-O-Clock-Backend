package com.ashish.shopoclock.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data // this annotation takes care of all the getters and setters and toString() method.
public class Review {

    @Id
    private String id;

    @NotBlank
    private String user;

    @NotBlank
    private String name;

    @NotNull
    @DecimalMax("5.00")
    @DecimalMin("0.00")
    private Double rating;

    @NotBlank
    private String comment;

    public Review(String user, String name, Double rating, String comment) {
        this.id = ObjectId.get().toHexString();
        this.user = user;
        this.name = name;
        this.rating = rating;
        this.comment = comment;
    }
}
