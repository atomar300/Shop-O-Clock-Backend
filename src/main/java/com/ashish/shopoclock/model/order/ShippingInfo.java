package com.ashish.shopoclock.model.order;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class ShippingInfo {

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String province;

    @NotBlank
    private String country;

    @NotNull
    private String pinCode;

    @NotNull
    @Range(max = 9999999999L, min = 1000000000L)
    private Long phoneNo;


}
