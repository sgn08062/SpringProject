package com.example.myFarm.stats;

import com.example.myFarm.command.MonthlyStatsVO;
import com.example.myFarm.command.StatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {
    @Autowired
    private StatsMapper statsMapper;

    @Override
    public StatsVO getTotalSales() {
        return statsMapper.getTotalSales();
    }

    @Override
    public List<MonthlyStatsVO> getMonthlyStats() {
        return statsMapper.getMonthlyStats();
    }
}
