package com.example.edmo.config;

import com.example.edmo.job.WeeklyReportJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

 //Quartz 定时任务配置
@Configuration
public class QuartzConfig {

    //周报任务详情
    @Bean
    public JobDetail weeklyReportJobDetail() {
        return JobBuilder.newJob(WeeklyReportJob.class)  // 指定执行哪个Job类
                .withIdentity("weeklyReportJob", "reportGroup")  // 任务唯一标识(名称, 分组)
                .storeDurably()  // 即使没有触发器也保留任务定义
                .build();
    }

    /*周报任务触发器 - 每周一早上9点执行
     Cron表达式: 0 0 9 ? * MON*/
    @Bean
    public Trigger weeklyReportTrigger(JobDetail weeklyReportJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(weeklyReportJobDetail)
                .withIdentity("weeklyReportTrigger", "reportGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 9 ? * MON"))
                .build();
    }
}

