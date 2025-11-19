package com.example.myFarm.stats;

import com.example.myFarm.command.MonthlyStatsVO;
import com.example.myFarm.command.StatsVO;

import java.util.List;

public interface StatsService {
    StatsVO getTotalSales();
    List<MonthlyStatsVO> getMonthlyStats();
}
