package com.example.myFarm.image;

import com.example.myFarm.command.ImageVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper {
    int insertImage(ImageVO vo);
}
