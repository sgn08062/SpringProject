package com.example.myFarm.stats;

import com.example.myFarm.command.StatsVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StatsMapper {
    StatsVO getTotalSales();
}
