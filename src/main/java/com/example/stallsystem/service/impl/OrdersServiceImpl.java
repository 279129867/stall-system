package com.example.stallsystem.service.impl;

import com.example.stallsystem.entity.Orders;
import com.example.stallsystem.mapper.OrdersMapper;
import com.example.stallsystem.service.OrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单主表 服务实现类
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

}
