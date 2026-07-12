package org.jeecg.modules.uav.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.ZoneId;

/**
 * 多线程池定时任务配置：精准隔离不同类型任务
 * @Author: 李海洋
 * @Date:   2026-02-28
 */
@Configuration
public class MultiScheduledTaskConfig {
    // 固定指定东八区时区，与服务器系统时区解耦
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 自定义定时任务线程池，Spring会自动替换默认的单线程调度器
     * 核心线程数建议3-5，满足多任务并行即可，避免资源浪费
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 核心线程数：根据项目定时任务数量调整，建议3-5
        scheduler.setPoolSize(5);
        // 线程名前缀：便于日志排查线程归属（如scheduled-task-1、scheduled-task-2）
        scheduler.setThreadNamePrefix("scheduled-task-");
        // 线程池关闭时，等待所有正在执行的任务完成后再销毁
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // 任务等待超时时间：超过60秒强制关闭，避免服务停不掉
        scheduler.setAwaitTerminationSeconds(60);
        // 初始化线程池（必须调用，否则线程池不生效）
        scheduler.initialize();
        return scheduler;
    }
    // 可选：配置Spring全局时区（覆盖服务器JVM默认时区，进一步解耦）
    @Bean
    public java.util.TimeZone systemDefaultTimeZone() {
        return java.util.TimeZone.getTimeZone(DEFAULT_ZONE);
    }
}