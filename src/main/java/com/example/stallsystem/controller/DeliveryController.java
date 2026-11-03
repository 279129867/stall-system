package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.Orders;
import com.example.stallsystem.service.OrdersService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配送员接口
 * 状态机：3待配送 -> 4配送中 -> 5已送达
 */
@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private final OrdersService ordersService;

    public DeliveryController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    /**
     * 待取订单（摊主已做好，等配送员取）
     */
    @GetMapping("/available")
    public Result<List<Orders>> available() {
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getStatus, (byte) 3).orderByDesc(Orders::getCreateTime);
        return Result.success(ordersService.list(qw));
    }

    /**
     * 配送中订单
     */
    @GetMapping("/my")
    public Result<List<Orders>> myDelivering() {
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getStatus, (byte) 4).orderByDesc(Orders::getCreateTime);
        return Result.success(ordersService.list(qw));
    }

    /**
     * 取货
     */
    @PostMapping("/{id}/pickup")
    public Result<String> pickup(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != 3) return Result.error("订单不在待配送状态");
        Orders upd = new Orders();
        upd.setId(id);
        upd.setStatus((byte) 4);
        ordersService.updateById(upd);
        return Result.success("已取货");
    }

    /**
     * 送达
     */
    @PostMapping("/{id}/deliver")
    public Result<String> deliver(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != 4) return Result.error("订单不在配送中");
        Orders upd = new Orders();
        upd.setId(id);
        upd.setStatus((byte) 5);
        ordersService.updateById(upd);
        return Result.success("已送达，订单完成");
    }
}
