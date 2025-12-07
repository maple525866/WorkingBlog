package com.iblog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author: fz
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataStatisticsComponent {
    private final ArticleService articleService;

    //每天执行一次，统计PV
    @Scheduled(cron = "1 0 0 * * ?")
    public void pvStatisticsPerDay() {
        log.info("定时任务 pvStatisticsPerDay 开始");
        articleService.pvStatisticsPerDay();
        log.info("定时任务 pvStatisticsPerDay 完成");
    }
}
