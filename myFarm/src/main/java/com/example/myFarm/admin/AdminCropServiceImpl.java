package com.example.myFarm.admin;


import com.example.myFarm.command.CropVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminCropServiceImpl implements AdminCropService {
    @Autowired
    private AdminCropMapper adminCropMapper;

    @Override
    public void addCrop(CropVO vo) {
        adminCropMapper.addCrop(vo);
    }
}
