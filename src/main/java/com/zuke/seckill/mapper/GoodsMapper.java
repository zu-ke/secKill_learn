package com.zuke.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zuke.seckill.entity.Goods;
import com.zuke.seckill.vo.GoodsVo;

import java.util.List;

public interface GoodsMapper extends BaseMapper<Goods> {

    //获取商品列表--秒杀
    List<GoodsVo> findGoodsVo();

    //获取指定商品详情
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
