package com.example.stallsystem.config;

import com.example.stallsystem.common.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行器配置：两个隔离的线程池。
 *
 * <p>为什么要分池：AI 调用一次 3~8 秒，如果和业务查询共用一个池，
 * 一波 AI 请求就能把池占满，导致下单/看板接口全部排队 —— 这叫故障扩散。
 * 所以慢调用和快调用必须物理隔离。</p>
 *
 * <p>为什么要 TaskDecorator：ThreadLocal（MDC traceId）不会自动跨线程，
 * 见 {@link MdcTaskDecorator}。</p>
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * AI 专用池：任务少但很慢，线程数小、队列短，避免堆积过多在途请求。
     */
    @Bean("aiExecutor")
    public ThreadPoolTaskExecutor aiExecutor() {
        return build("ai-", 4, 8, 200);
    }

    /**
     * 业务并行池：看板聚合这类短任务，线程数可以多一点。
     */
    @Bean("bizExecutor")
    public ThreadPoolTaskExecutor bizExecutor() {
        return build("biz-", 8, 16, 1000);
    }

    private ThreadPoolTaskExecutor build(String prefix, int core, int max, int queue) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(max);
        executor.setQueueCapacity(queue);              // 必须有界，否则等于内存无限
        executor.setThreadNamePrefix(prefix);          // 线程名带上池标识，日志/线程栈一眼能看出来
        executor.setTaskDecorator(new MdcTaskDecorator());
        // 池满时回到调用线程执行：形成反压，宁可让上游慢一点，也不要丢任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 优雅停机：等在途任务跑完再退出，避免异步任务被硬杀
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /** @Async 不指定池名时默认走这里 */
    @Override
    public Executor getAsyncExecutor() {
        return bizExecutor();
    }

    /**
     * 异步任务的异常不会抛回调用方，必须在这里统一兜住并记录。
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("[async-error] {} 执行失败", method.getName(), ex);
    }
}
