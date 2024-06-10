package com.zuke.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuke.seckill.entity.Goods;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.mapper.GoodsMapper;
import com.zuke.seckill.mapper.UserMapper;
import com.zuke.seckill.service.GoodsService;
import com.zuke.seckill.service.UserService;
import com.zuke.seckill.vo.GoodsVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;

    //秒杀商品列表
    @Override
    public List<GoodsVo> findGoodsVo() {
        List<GoodsVo> goodsVo = goodsMapper.findGoodsVo();
        return goodsVo;
    }

    //获取指定商品详情
    @Override
    public GoodsVo toDetailByGoodsId(Long goodsId) {
        return goodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}
