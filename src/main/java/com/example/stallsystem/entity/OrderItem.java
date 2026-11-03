package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 订单明细表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
@TableName("order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（冗余快照）
     */
    private String productName;

    /**
     * 下单时单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;
}
