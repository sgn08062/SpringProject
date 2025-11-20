package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageVO {
    private long imageId;
    private String imageUrl;
    private String imageType;
    private long itemId;
}
