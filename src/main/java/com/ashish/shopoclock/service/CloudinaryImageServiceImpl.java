package com.ashish.shopoclock.service;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryImageServiceImpl implements CloudinaryImageService{

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public Map upload(MultipartFile file) {
        try {
            Map data = this.cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "Shop O'Clock"));
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Image uploading failed!!!");
        }
    }

    public Map upload(byte [] file) {
        try {
            Map data = this.cloudinary.uploader().upload(file, Map.of("folder", "Shop O'Clock"));
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Image uploading failed!!!");
        }
    }

    public void delete(String public_id){
        try {
            this.cloudinary.uploader().destroy(public_id, Map.of("folder", "Shop O'Clock"));
        } catch (IOException e) {
            throw new RuntimeException("Image deletion failed!!!");
        }
    }
}
