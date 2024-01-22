package com.ashish.shopoclock.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;



@Data // this annotation takes care of all the getters and setters and toString() method.
public class Image {

    @Id
    private String id;

    @NotBlank
    private String public_id;

    @NotBlank
    private String url;

    public Image(String public_id, String url){
        this.id = ObjectId.get().toHexString();
        this.public_id = public_id;
        this.url = url;
    }

}
