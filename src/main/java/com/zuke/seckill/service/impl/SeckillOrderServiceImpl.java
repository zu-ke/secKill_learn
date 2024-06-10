package com.zuke.seckill.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuke.seckill.entity.SeckillOrder;
import com.zuke.seckill.mapper.SeckillOrderMapper;
import com.zuke.seckill.service.SeckillOrderService;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderServiceImpl
        extends ServiceImpl<SeckillOrderMapper, SeckillOrder>
        implements SeckillOrderService {
}
