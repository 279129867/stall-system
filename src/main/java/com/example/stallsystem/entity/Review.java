package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 评价表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
public class Review implements Serializable {

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
     * 消费者ID
     */
    private Long consumerId;

    /**
     * 摊位ID
     */
    private Long stallId;

    /**
     * 评分1-5
     */
    private Byte rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
