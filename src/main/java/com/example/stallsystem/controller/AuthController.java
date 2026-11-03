package com.example.stallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stallsystem.common.Result;
import com.example.stallsystem.dto.LoginRequest;
import com.example.stallsystem.entity.Admin;
import com.example.stallsystem.entity.Consumer;
import com.example.stallsystem.entity.StallOwner;
import com.example.stallsystem.security.JwtUtil;
import com.example.stallsystem.service.AdminService;
import com.example.stallsystem.service.ConsumerService;
import com.example.stallsystem.service.StallOwnerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证接口：登录
 * 支持三种角色：admin / stallowner / consumer
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AdminService adminService;
    private final StallOwnerService stallOwnerService;
    private final ConsumerService consumerService;

    public AuthController(JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          AdminService adminService,
                          StallOwnerService stallOwnerService,
                          ConsumerService consumerService) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
        this.stallOwnerService = stallOwnerService;
        this.consumerService = consumerService;
    }

    /**
     * 登录
     * body: {"username":"admin","password":"admin123","role":"admin"}
     * role: admin / stallowner / consumer
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest req) {
        String username = req.getUsername();
        String password = req.getPassword();
        String role = req.getRole();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Result.error(400, "请输入账号和密码");
        }

        String dbPassword = null;
        String nickname = null;
        Long userId = null;

        switch (role == null ? "" : role) {
            case "admin": {
                LambdaQueryWrapper<Admin> qw = new LambdaQueryWrapper<>();
                qw.eq(Admin::getUsername, username);
                Admin admin = adminService.getOne(qw);
                if (admin == null) return Result.error(401, "账号或密码错误");
                dbPassword = admin.getPassword();
                nickname = admin.getRealName();
                userId = admin.getId();
                break;
            }
            case "stallowner": {
                LambdaQueryWrapper<StallOwner> qw = new LambdaQueryWrapper<>();
                qw.eq(StallOwner::getUsername, username);
                StallOwner owner = stallOwnerService.getOne(qw);
                if (owner == null) return Result.error(401, "账号或密码错误");
                if (owner.getStatus() != null && owner.getStatus() == 0) {
                    return Result.error(403, "账号已被禁用，请联系管理员");
                }
                dbPassword = owner.getPassword();
                nickname = owner.getRealName();
                userId = owner.getId();
                break;
            }
            case "consumer": {
                LambdaQueryWrapper<Consumer> qw = new LambdaQueryWrapper<>();
                qw.eq(Consumer::getUsername, username);
                Consumer c = consumerService.getOne(qw);
                if (c == null) return Result.error(401, "账号或密码错误");
                dbPassword = c.getPassword();
                nickname = c.getNickname();
                userId = c.getId();
                break;
            }
            default:
                return Result.error(400, "角色参数错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(password, dbPassword)) {
            return Result.error(401, "账号或密码错误");
        }

        // 生成 token
        String token = jwtUtil.generateToken(username, role);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        data.put("nickname", nickname);
        data.put("role", role);
        data.put("userId", String.valueOf(userId));
        return Result.success(data);
    }
}
