package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 商品分类表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
@TableName("product_category")
public class ProductCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
