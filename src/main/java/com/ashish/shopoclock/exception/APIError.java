package com.ashish.shopoclock.exception;
import lombok.Builder;
import lombok.Data;

@Data
public class APIError {

    private boolean success = false;
    private String message;

    public APIError(String message){
        this.message = message;
    }

}
