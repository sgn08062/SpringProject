package com.example.myFarm.image;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    int saveItemImages(long itemId, MultipartFile mainImage, List<MultipartFile> detailImages);
}
