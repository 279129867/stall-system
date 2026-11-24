package com.example.stallsystem.common;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 把调用线程的 MDC（traceId 等）传递给线程池里的工作线程。
 *
 * 为什么要它：ThreadLocal 不会自动跨线程传播，异步任务里拿不到 traceId，
 * 日志就会断开，异步链路等于"黑盒"。加了这层之后，一条请求在
 * http-nio-* / ai-* / biz-* 三个线程里的日志都会带同一个 traceId。
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (context != null) {
                MDC.setContextMap(context);
            }
            try {
                runnable.run();
            } finally {
                // 线程是复用的，用完必须还原，否则会污染下一个任务
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}
