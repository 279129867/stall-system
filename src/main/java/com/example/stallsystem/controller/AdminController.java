package com.example.stallsystem.controller;

import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.Consumer;
import com.example.stallsystem.entity.StallOwner;
import com.example.stallsystem.mapper.OrdersMapper;
import com.example.stallsystem.service.ConsumerService;
import com.example.stallsystem.service.OrdersService;
import com.example.stallsystem.service.StallOwnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 管理员后台接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StallOwnerService stallOwnerService;
    private final ConsumerService consumerService;
    private final OrdersService ordersService;
    private final OrdersMapper ordersMapper;
    private final ThreadPoolTaskExecutor bizExecutor;
    private final ThreadPoolTaskExecutor aiExecutor;

    /** 看板降级快照：并行聚合超时/失败时，退回上一次成功的结果，保证接口不 500 */
    private final AtomicReference<Map<String, Object>> lastStats = new AtomicReference<>(
            Map.of("stallCount", 0L, "consumerCount", 0L, "orderCount", 0L, "revenue", BigDecimal.ZERO));

    public AdminController(StallOwnerService stallOwnerService,
                           ConsumerService consumerService,
                           OrdersService ordersService,
                           OrdersMapper ordersMapper,
                           @Qualifier("bizExecutor") ThreadPoolTaskExecutor bizExecutor,
                           @Qualifier("aiExecutor") ThreadPoolTaskExecutor aiExecutor) {
        this.stallOwnerService = stallOwnerService;
        this.consumerService = consumerService;
        this.ordersService = ordersService;
        this.ordersMapper = ordersMapper;
        this.bizExecutor = bizExecutor;
        this.aiExecutor = aiExecutor;
    }

    /**
     * 数据看板
     *
     * <p>4 个互相独立的查询并行扇出，统一 500ms 时间预算；任何一路超时/异常，
     * 整体降级为上一次成功的快照，而不是让整个接口失败。</p>
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long start = System.currentTimeMillis();

        CompletableFuture<Long> stallFuture = CompletableFuture
                .supplyAsync(stallOwnerService::count, bizExecutor);
        CompletableFuture<Long> consumerFuture = CompletableFuture
                .supplyAsync(consumerService::count, bizExecutor);
        CompletableFuture<Long> orderFuture = CompletableFuture
                .supplyAsync(ordersService::count, bizExecutor);
        CompletableFuture<BigDecimal> revenueFuture = CompletableFuture
                .supplyAsync(ordersMapper::sumRevenue, bizExecutor);

        try {
            CompletableFuture
                    .allOf(stallFuture, consumerFuture, orderFuture, revenueFuture)
                    .orTimeout(500, TimeUnit.MILLISECONDS)
                    .join();

            Map<String, Object> data = Map.of(
                    "stallCount", stallFuture.join(),
                    "consumerCount", consumerFuture.join(),
                    "orderCount", orderFuture.join(),
                    "revenue", revenueFuture.join());

            lastStats.set(data);
            log.info("[stats] 4 路并行聚合完成，耗时 {}ms", System.currentTimeMillis() - start);
            return Result.success(data);
        } catch (Exception e) {
            log.warn("[stats] 并行聚合失败或超时（{}ms），降级为上次快照：{}",
                    System.currentTimeMillis() - start, e.toString());
            return Result.success(lastStats.get());
        }
    }

    /**
     * 线程池水位（压测/答辩演示用）
     * GET /api/admin/threadpool
     */
    @GetMapping("/threadpool")
    public Result<List<Map<String, Object>>> threadpool() {
        return Result.success(List.of(
                snapshot("ai", aiExecutor),
                snapshot("biz", bizExecutor)));
    }

    private Map<String, Object> snapshot(String name, ThreadPoolTaskExecutor executor) {
        ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("poolSize", pool.getPoolSize());
        m.put("active", pool.getActiveCount());
        m.put("queueSize", pool.getQueue().size());
        m.put("completed", pool.getCompletedTaskCount());
        return m;
    }

    /**
     * 摊主列表（含禁用/启用）
     */
    @GetMapping("/stalls")
    public Result<List<StallOwner>> stalls() {
        return Result.success(stallOwnerService.list());
    }

    /**
     * 禁用/启用摊主
     * PATCH /api/admin/stall/{id}/status?status=0
     */
    @PatchMapping("/stall/{id}/status")
    public Result<String> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        StallOwner owner = new StallOwner();
        owner.setId(id);
        owner.setStatus(status.byteValue());
        stallOwnerService.updateById(owner);
        return Result.success(status == 0 ? "已禁用" : "已启用");
    }

    /**
     * 消费者列表
     */
    @GetMapping("/consumers")
    public Result<List<Consumer>> consumers() {
        return Result.success(consumerService.list());
    }
}
