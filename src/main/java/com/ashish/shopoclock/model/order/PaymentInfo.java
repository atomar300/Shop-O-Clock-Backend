package com.ashish.shopoclock.model.order;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentInfo {

    @NotBlank
    private String id;

    @NotBlank
    private String status;

}
