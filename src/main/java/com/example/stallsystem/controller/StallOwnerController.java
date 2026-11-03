package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.ShopInfo;
import com.example.stallsystem.entity.StallOwner;
import com.example.stallsystem.service.ShopInfoService;
import com.example.stallsystem.service.StallOwnerService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class StallOwnerController {

    @Resource
    private StallOwnerService stallOwnerService;

    @Resource
    private ShopInfoService shopInfoService;

    @GetMapping("/list")
    public Result<List<StallOwner>> list() {
        return Result.success(stallOwnerService.list());
    }

    /**
     * 获取小店信息
     * GET /api/shop/info?stallId=1
     */
    @GetMapping("/info")
    public Result<ShopInfo> info(@RequestParam Long stallId) {
        LambdaQueryWrapper<ShopInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(ShopInfo::getStallId, stallId).last("LIMIT 1");
        return Result.success(shopInfoService.getOne(qw));
    }

    /**
     * 保存小店信息
     * PUT /api/shop/info
     */
    @PutMapping("/info")
    public Result<String> saveInfo(@RequestBody ShopInfo info) {
        LambdaQueryWrapper<ShopInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(ShopInfo::getStallId, info.getStallId()).last("LIMIT 1");
        ShopInfo exist = shopInfoService.getOne(qw);
        if (exist != null) {
            info.setId(exist.getId());
            shopInfoService.updateById(info);
        } else {
            shopInfoService.save(info);
        }
        return Result.success("保存成功");
    }
}
