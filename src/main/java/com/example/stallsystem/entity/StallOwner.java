package com.example.stallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 摊主表
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Getter
@Setter
@TableName("stall_owner")
public class StallOwner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 密码（BCrypt 加密存储）
     */
    @JsonIgnore
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 身份证号
     */
    @JsonIgnore
    private String idCard;

    /**
     * 状态：1-正常 0-禁用
     */
    private Byte status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
