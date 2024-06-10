package com.zuke.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuke.seckill.entity.SeckillGoods;
import com.zuke.seckill.mapper.SeckillGoodsMapper;
import com.zuke.seckill.service.SeckillGoodsService;
import org.springframework.stereotype.Service;

@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsService {
}
