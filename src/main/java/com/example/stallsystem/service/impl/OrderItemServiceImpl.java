package com.example.stallsystem.service.impl;

import com.example.stallsystem.entity.OrderItem;
import com.example.stallsystem.mapper.OrderItemMapper;
import com.example.stallsystem.service.OrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

}
