package com.example.myFarm.stats;

import com.example.myFarm.command.StatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatsServiceImpl implements StatsService {
    @Autowired
    private StatsMapper statsMapper;

    @Override
    public StatsVO getTotalSales() {
        return statsMapper.getTotalSales();
    }
}
