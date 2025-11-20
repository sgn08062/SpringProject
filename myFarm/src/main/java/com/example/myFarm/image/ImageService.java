package com.example.myFarm.image;

import com.example.myFarm.command.ImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    void saveItemImages(long itemId, MultipartFile mainImage, List<MultipartFile> detailImages);
    // 기존 이미지가 삭제되었을 때
    void deleteImagesByIds(List<Long> idsToDelete);
    // 새로운 이미지가 들어왔을 때
    void appendNewImages(long itemId, MultipartFile mainImage, List<MultipartFile> detailImages);
    List<ImageVO> getImagesByItemId(long itemId);
}
