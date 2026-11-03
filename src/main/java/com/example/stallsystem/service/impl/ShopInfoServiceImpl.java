package com.example.stallsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.stallsystem.entity.ShopInfo;
import com.example.stallsystem.mapper.ShopInfoMapper;
import com.example.stallsystem.service.ShopInfoService;
import org.springframework.stereotype.Service;

@Service
public class ShopInfoServiceImpl extends ServiceImpl<ShopInfoMapper, ShopInfo> implements ShopInfoService {}
