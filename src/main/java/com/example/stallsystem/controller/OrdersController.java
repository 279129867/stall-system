package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.OrderItem;
import com.example.stallsystem.entity.Orders;
import com.example.stallsystem.entity.Product;
import com.example.stallsystem.service.OrderItemService;
import com.example.stallsystem.service.OrdersService;
import com.example.stallsystem.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 订单接口
 *
 * 状态机：0待付款 -> 1待接单 -> 2制作中 -> 3已完成
 *                                  ↘ 4已取消
 */
@RestController
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrdersService ordersService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    public OrdersController(OrdersService ordersService,
                            ProductService productService,
                            OrderItemService orderItemService) {
        this.ordersService = ordersService;
        this.productService = productService;
        this.orderItemService = orderItemService;
    }

    /**
     * 摊主订单大厅
     * GET /api/orders/hall?stallId=1&status=1
     */
    @GetMapping("/hall")
    public Result<List<Orders>> hall(@RequestParam Long stallId,
                                     @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getStallId, stallId);
        if (status != null) {
            qw.eq(Orders::getStatus, status.byteValue());
        }
        qw.orderByDesc(Orders::getCreateTime);
        return Result.success(ordersService.list(qw));
    }

    /**
     * 消费者查我的订单
     * GET /api/orders/my?consumerId=1
     */
    @GetMapping("/my")
    public Result<List<Orders>> myOrders(@RequestParam Long consumerId) {
        LambdaQueryWrapper<Orders> qw = new LambdaQueryWrapper<>();
        qw.eq(Orders::getConsumerId, consumerId).orderByDesc(Orders::getCreateTime);
        return Result.success(ordersService.list(qw));
    }

    /**
     * 消费者下单
     * POST /api/orders
     * body: { "consumerId":1, "stallId":1, "productId":1, "quantity":2, "remark":"少辣" }
     */
    @PostMapping
    public Result<Orders> create(@RequestBody Map<String, Object> body) {
        Long consumerId = Long.valueOf(body.get("consumerId").toString());
        Long stallId = Long.valueOf(body.get("stallId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer quantity = body.get("quantity") == null ? 1 : Integer.valueOf(body.get("quantity").toString());
        String remark = body.get("remark") == null ? "" : body.get("remark").toString();

        Product product = productService.getById(productId);
        if (product == null) return Result.error("商品不存在");
        if (product.getStatus() != 1) return Result.error("商品已下架");

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        Orders order = new Orders();
        order.setOrderNo("ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setConsumerId(consumerId);
        order.setStallId(stallId);
        order.setTotalAmount(total);
        order.setStatus((byte) 1); // 模拟已支付，直接待接单
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.now());
        ordersService.save(order);

        // 写订单明细
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(productId);
        item.setProductName(product.getProductName());
        item.setPrice(product.getPrice());
        item.setQuantity(quantity);
        orderItemService.save(item);

        return Result.success(order);
    }

    /**
     * 摊主接单
     */
    @PatchMapping("/{id}/accept")
    public Result<String> accept(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != 1) return Result.error("当前状态不可接单");
        Orders upd = new Orders();
        upd.setId(id);
        upd.setStatus((byte) 2);
        ordersService.updateById(upd);
        return Result.success("接单成功");
    }

    @PatchMapping("/{id}/start")
    public Result<String> start(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != 2) return Result.error("订单未处于制作中");
        return Result.success("已开始制作");
    }

    /**
     * 摊主完成
     * deliveryType: self=商家自配送(直接完成) / courier=转配送员(待配送)
     */
    @PatchMapping("/{id}/finish")
    public Result<String> finish(@PathVariable Long id,
                                 @RequestParam(defaultValue = "self") String deliveryType) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != 2) return Result.error("只有制作中的订单才能完成");
        Orders upd = new Orders();
        upd.setId(id);
        // self=5已完成, courier=3待配送
        upd.setStatus("courier".equals(deliveryType) ? (byte) 3 : (byte) 5);
        ordersService.updateById(upd);
        return Result.success("courier".equals(deliveryType) ? "已转配送员" : "订单已完成");
    }

    @PatchMapping("/{id}/cancel")
    public Result<String> cancel(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.error("订单不存在");
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            return Result.error("当前状态不可取消");
        }
        Orders upd = new Orders();
        upd.setId(id);
        upd.setStatus((byte) 4);
        ordersService.updateById(upd);
        return Result.success("订单已取消");
    }
}
