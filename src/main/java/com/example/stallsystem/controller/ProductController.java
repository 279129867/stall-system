package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.entity.Product;
import com.example.stallsystem.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口（TC8 摊主我的小店）
 */
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表：按摊位查
     * GET /api/product/list?stallId=1
     */
    @GetMapping("/list")
    public Result<List<Product>> list(@RequestParam Long stallId) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<>();
        qw.eq(Product::getStallId, stallId).orderByDesc(Product::getCreateTime);
        return Result.success(productService.list(qw));
    }

    /**
     * 新增商品
     * POST /api/product
     */
    @PostMapping
    public Result<String> add(@RequestBody Product product) {
        if (product.getProductName() == null || product.getProductName().isBlank()) {
            return Result.error("商品名称不能为空");
        }
        product.setId(null);
        if (product.getStatus() == null) product.setStatus((byte) 1);
        productService.save(product);
        return Result.success("添加成功");
    }

    /**
     * 编辑商品
     * PUT /api/product
     */
    @PutMapping
    public Result<String> update(@RequestBody Product product) {
        if (product.getId() == null) return Result.error("id 不能为空");
        productService.updateById(product);
        return Result.success("修改成功");
    }

    /**
     * 上架
     * PATCH /api/product/{id}/on
     */
    @PatchMapping("/{id}/on")
    public Result<String> onShelf(@PathVariable Long id) {
        Product p = new Product();
        p.setId(id);
        p.setStatus((byte) 1);
        productService.updateById(p);
        return Result.success("已上架");
    }

    /**
     * 下架
     * PATCH /api/product/{id}/off
     */
    @PatchMapping("/{id}/off")
    public Result<String> offShelf(@PathVariable Long id) {
        Product p = new Product();
        p.setId(id);
        p.setStatus((byte) 0);
        productService.updateById(p);
        return Result.success("已下架");
    }

    /**
     * 删除商品
     * DELETE /api/product/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        productService.removeById(id);
        return Result.success("已删除");
    }
}
