package com.example.myFarm.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyStatsVO {
    long monthlyOrder;
    long monthlyTotal;
    long monthlyAvg;
}
