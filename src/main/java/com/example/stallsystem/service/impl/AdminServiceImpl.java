package com.example.stallsystem.service.impl;

import com.example.stallsystem.entity.Admin;
import com.example.stallsystem.mapper.AdminMapper;
import com.example.stallsystem.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 管理员表 服务实现类
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

}
