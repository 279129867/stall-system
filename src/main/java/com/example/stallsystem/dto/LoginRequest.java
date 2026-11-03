package com.example.stallsystem.dto;

import lombok.Data;

/**
 * 登录请求参数
 */
@Data
public class LoginRequest {
    /** 账号 */
    private String username;
    /** 密码 */
    private String password;
    /** 角色：admin / stall_owner / consumer */
    private String role;
}
