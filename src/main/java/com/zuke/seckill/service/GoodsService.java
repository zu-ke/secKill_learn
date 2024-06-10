package com.zuke.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zuke.seckill.entity.Goods;
import com.zuke.seckill.vo.GoodsVo;

import java.util.List;

public interface GoodsService extends IService<Goods> {

    //秒杀商品列表
    List<GoodsVo> findGoodsVo();

    //获取指定商品详情
    GoodsVo toDetailByGoodsId(Long goodsId);
}
