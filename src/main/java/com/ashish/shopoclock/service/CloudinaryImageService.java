package com.ashish.shopoclock.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryImageService {

    public Map upload(MultipartFile file);

    public Map upload(byte[] data);

    public void delete(String public_id);
}
