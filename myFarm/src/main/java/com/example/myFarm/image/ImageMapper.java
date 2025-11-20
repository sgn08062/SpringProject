package com.example.myFarm.image;

import com.example.myFarm.command.ImageVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageMapper {
    int insertImage(ImageVO vo);
    int deleteImagesByIds(List<Long> idsToDelete);
    List<ImageVO> selectImagesByItemId(long itemId);
}
