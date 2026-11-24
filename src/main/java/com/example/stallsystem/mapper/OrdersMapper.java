package com.example.stallsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.stallsystem.entity.Orders;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 订单主表 Mapper 接口
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 已完成订单总金额（status=3）。
     *
     * <p>原来的写法是把所有已完成订单 list 到内存里 reduce，
     * 数据量一大就是 OOM 隐患；聚合交给数据库，只回传一个数。</p>
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 3")
    BigDecimal sumRevenue();
}
