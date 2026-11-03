package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单表
 */
@Getter
@Setter
public class Bill implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String billNo;

    private Long stallId;

    private BigDecimal amount;

    private String billType;

    private String period;

    /** 0未支付 1已支付 */
    private Byte status;

    private LocalDateTime payTime;

    private String payChannel;

    private LocalDateTime createTime;
}
