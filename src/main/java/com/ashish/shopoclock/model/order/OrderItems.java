package com.ashish.shopoclock.model.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

@Data
public class OrderItems {

    @Id
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private Double price;

    @NotNull
    private Integer quantity;

    @NotBlank
    private String image;

    @NotBlank
    private String product;

    public OrderItems() {
        this.id = ObjectId.get().toHexString();
    }
}
