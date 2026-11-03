package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.Orders;
import com.example.stallsystem.entity.StallOwner;
import com.example.stallsystem.entity.Consumer;
import com.example.stallsystem.service.OrdersService;
import com.example.stallsystem.service.StallOwnerService;
import com.example.stallsystem.service.ConsumerService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员后台接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final StallOwnerService stallOwnerService;
    private final ConsumerService consumerService;
    private final OrdersService ordersService;

    public AdminController(StallOwnerService stallOwnerService,
                           ConsumerService consumerService,
                           OrdersService ordersService) {
        this.stallOwnerService = stallOwnerService;
        this.consumerService = consumerService;
        this.ordersService = ordersService;
    }

    /**
     * 数据看板
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long stallCount = stallOwnerService.count();
        long consumerCount = consumerService.count();
        long orderCount = ordersService.count();

        // 已完成订单总金额
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getStatus, 3);
        List<Orders> doneOrders = ordersService.list(qw);
        BigDecimal revenue = doneOrders.stream()
                .map(Orders::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("stallCount", stallCount);
        data.put("consumerCount", consumerCount);
        data.put("orderCount", orderCount);
        data.put("revenue", revenue);
        return Result.success(data);
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
