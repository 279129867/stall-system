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
 * 商品表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 所属摊位ID
     */
    private Long stallId;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 状态：1-上架 0-下架
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
