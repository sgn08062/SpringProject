package com.example.myFarm.stats;

import com.example.myFarm.command.MonthlyStatsVO;
import com.example.myFarm.command.StatsVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatsMapper {
    StatsVO getTotalSales();
    List<MonthlyStatsVO> getMonthlyStats();
}
