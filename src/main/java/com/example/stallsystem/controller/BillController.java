package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.Bill;
import com.example.stallsystem.service.BillService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线缴费接口（TC5）
 *
 * 流程：查未付账单 -> 发起支付(出二维码) -> 扫码 -> 回调更新状态
 */
@RestController
@RequestMapping("/api/bill")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    /**
     * 查账单列表
     * GET /api/bill?stallId=1&paid=0   未支付
     * GET /api/bill?stallId=1&paid=1   已支付
     */
    @GetMapping
    public Result<List<Bill>> list(@RequestParam Long stallId,
                                   @RequestParam(required = false) Integer paid) {
        LambdaQueryWrapper<Bill> qw = new LambdaQueryWrapper<>();
        qw.eq(Bill::getStallId, stallId);
        if (paid != null) {
            qw.eq(Bill::getStatus, paid.byteValue());
        }
        qw.orderByDesc(Bill::getCreateTime);
        return Result.success(billService.list(qw));
    }

    /**
     * 发起支付（沙箱）：返回模拟支付二维码
     * POST /api/bill/{id}/pay-sandbox
     * body: { "amount": 150.00 }  前端篡改金额在这里校验
     */
    @PostMapping("/{id}/pay-sandbox")
    public Result<Map<String, Object>> paySandbox(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        Bill bill = billService.getById(id);
        if (bill == null) return Result.error("账单不存在");
        if (bill.getStatus() == 1) return Result.error("该账单已支付");

        // TC5-07 金额校验：服务端以账单原始金额为准
        if (body != null && body.containsKey("amount")) {
            BigDecimal clientAmount = new BigDecimal(body.get("amount").toString());
            if (clientAmount.compareTo(bill.getAmount()) != 0) {
                return Result.error("支付金额与账单不符");
            }
        }

        // 生成模拟支付二维码（真实场景接微信/支付宝）
        Map<String, Object> data = new HashMap<>();
        data.put("billId", id);
        data.put("billNo", bill.getBillNo());
        data.put("amount", bill.getAmount());
        data.put("qrCodeUrl", "https://pay.example.com/qr/" + bill.getBillNo());
        data.put("expireSeconds", 300);
        return Result.success(data);
    }

    /**
     * 支付回调（模拟支付完成后调用）
     * POST /api/bill/{id}/pay-callback
     */
    @PostMapping("/{id}/pay-callback")
    public Result<String> payCallback(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, String> body) {
        Bill bill = billService.getById(id);
        if (bill == null) return Result.error("账单不存在");
        if (bill.getStatus() == 1) return Result.error("该账单已支付");

        Bill upd = new Bill();
        upd.setId(id);
        upd.setStatus((byte) 1);
        upd.setPayTime(LocalDateTime.now());
        upd.setPayChannel(body != null && body.get("channel") != null ? body.get("channel") : "wechat");
        billService.updateById(upd);
        return Result.success("支付成功");
    }

    /**
     * 取消支付（关闭二维码弹窗，账单状态不变）
     * POST /api/bill/{id}/cancel-pay
     */
    @PostMapping("/{id}/cancel-pay")
    public Result<String> cancelPay(@PathVariable Long id) {
        Bill bill = billService.getById(id);
        if (bill == null) return Result.error("账单不存在");
        // 不改状态，前端直接关弹窗即可
        return Result.success("已取消支付");
    }
}
