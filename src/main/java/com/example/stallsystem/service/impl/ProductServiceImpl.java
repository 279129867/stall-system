package com.example.stallsystem.service.impl;

import com.example.stallsystem.entity.Product;
import com.example.stallsystem.mapper.ProductMapper;
import com.example.stallsystem.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

}
