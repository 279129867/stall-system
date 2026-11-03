package com.example.stallsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.stallsystem.entity.Bill;
import com.example.stallsystem.mapper.BillMapper;
import com.example.stallsystem.service.BillService;
import org.springframework.stereotype.Service;

@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements BillService {
}
