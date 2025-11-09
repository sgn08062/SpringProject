package com.example.myFarm.admin;


import com.example.myFarm.command.CropVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminCropServiceImpl implements AdminCropService {
    @Autowired
    private AdminCropMapper adminCropMapper;

    @Override
    public int addCrop(CropVO vo) {
        return adminCropMapper.addCrop(vo);
    }

    @Override
    public int deleteCrop(long cropId) {
        return adminCropMapper.deleteCrop(cropId);
    }

    @Override
    public int enableCrop(long cropId) {
        return adminCropMapper.enableCrop(cropId);
    }

    @Override
    public int disableCrop(long cropId) {
        return adminCropMapper.disableCrop(cropId);
    }
}
