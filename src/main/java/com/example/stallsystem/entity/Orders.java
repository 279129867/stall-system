package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 订单主表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 消费者ID
     */
    private Long consumerId;

    /**
     * 摊位ID
     */
    private Long stallId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态：0-待付款 1-已付款 2-已完成 3-已取消
     */
    private Byte status;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
