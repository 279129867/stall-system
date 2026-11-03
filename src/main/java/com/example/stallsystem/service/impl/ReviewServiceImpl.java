package com.example.stallsystem.service.impl;

import com.example.stallsystem.entity.Review;
import com.example.stallsystem.mapper.ReviewMapper;
import com.example.stallsystem.service.ReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 评价表 服务实现类
 * </p>
 *
 * @author example
 * @since 2026-10-05
 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

}
