package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.Product;
import com.example.stallsystem.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * AI 导购：根据用户关键词推荐商品
 */
@RestController
@RequestMapping("/api/product")
public class ProductRecommendController {

    private final ProductService productService;

    public ProductRecommendController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品推荐
     * GET /api/product/recommend?keyword=我想买奶茶
     */
    @GetMapping("/recommend")
    public Result<List<Product>> recommend(@RequestParam String keyword) {
        // 购物关键词列表
        List<String> words = Arrays.asList(
            "奶茶","茶","咖啡","果汁","水果","芒果","车厘子","饮","小吃","面","饭","饼","冰粉","甜品","零食"
        );

        // 从用户输入里提取可能匹配的词
        String hitWord = words.stream().filter(keyword::contains).findFirst().orElse(null);

        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getStatus, 1);

        if (hitWord != null) {
            qw.like(Product::getProductName, hitWord);
        }
        qw.orderByDesc(Product::getPrice);
        qw.last("LIMIT 5");

        List<Product> list = productService.list(qw);

        // 兜底：匹配不到返回所有上架商品
        if (list.isEmpty()) {
            LambdaQueryWrapper<Product> qw2 = new LambdaQueryWrapper<>();
            qw2.eq(Product::getStatus, 1).last("LIMIT 5");
            list = productService.list(qw2);
        }
        return Result.success(list);
    }
}
