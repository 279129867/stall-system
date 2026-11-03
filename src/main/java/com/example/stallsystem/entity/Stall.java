package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 摊位表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
public class Stall implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 摊位编号
     */
    private String stallNo;

    /**
     * 摊位名称
     */
    private String stallName;

    /**
     * 摊位位置
     */
    private String location;

    /**
     * 所属摊主ID
     */
    private Long ownerId;

    /**
     * 摊位描述
     */
    private String description;

    /**
     * 状态：1-营业 0-打烊
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
