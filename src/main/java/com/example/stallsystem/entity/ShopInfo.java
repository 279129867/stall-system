package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("shop_info")
public class ShopInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long stallId;
    private String name;
    private String openTime;
    private BigDecimal minPrice;
    private String notice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
