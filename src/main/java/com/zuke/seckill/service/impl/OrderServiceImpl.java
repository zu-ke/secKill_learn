package com.zuke.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zuke.seckill.entity.Order;
import com.zuke.seckill.entity.SeckillGoods;
import com.zuke.seckill.entity.SeckillOrder;
import com.zuke.seckill.entity.User;
import com.zuke.seckill.mapper.OrderMapper;
import com.zuke.seckill.mapper.SeckillOrderMapper;
import com.zuke.seckill.service.OrderService;
import com.zuke.seckill.service.SeckillGoodsService;
import com.zuke.seckill.service.SeckillOrderService;
import com.zuke.seckill.util.MD5Util;
import com.zuke.seckill.util.UUIDUtil;
import com.zuke.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl
        extends ServiceImpl<OrderMapper, Order>
        implements OrderService {

    @Resource
    private SeckillGoodsService seckillGoodsService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public Order secKill(User user, GoodsVo goodsVo) {
        //查询商品库存，并减一
        SeckillGoods secKillGoods = seckillGoodsService.getOne(
                new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId())
        );
        //完成一个秒杀【不具有原子性，并发会有问题，后面优化】
        //secKillGoods.setStockCount(secKillGoods.getStockCount() - 1);
        //seckillGoodsService.updateById(secKillGoods);

        //优化
        //MySQL在默认的事务隔离（REPEATABLE-READ）：执行update语句时，会在事务中锁定要更新的行，防止其他会话在同一行执行update或者delete语句
        //如果在短时间内，大量抢购冲击DB，造成洪峰，容易压垮数据库，
        //使用redis完成预见库存，如果没有库存了，直接返回，减小对DB的压力。在【SecKillController】完成该操作
        System.out.println("秒杀 update 执行");
        boolean result = seckillGoodsService.update(
                new UpdateWrapper<SeckillGoods>()
                        .setSql("stock_count=stock_count-1")
                        .eq("goods_id", goodsVo.getId())
                        .gt("stock_count", 0)
        );
        if (!result) {
            return null;
        }


        //生成普通订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setGoodsName(goodsVo.getGoodsName());
        order.setDeliveryAddrId(0L);
        order.setGoodsCount(1);
        order.setGoodsPrice(secKillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        //保存order信息
        orderMapper.insert(order);

        //生成秒杀商品订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goodsVo.getId());
        //上面添加order时，mybatisPlus会得到新添加的id
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        //保存secKillOrder信息
        seckillOrderMapper.insert(seckillOrder);

        //增加代码：将生成的秒杀订单存入到redis，这样在查询某个用户是否秒杀了该商品时【SecKillController】，直接到redis查询，可以起到优化效果
        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId(), seckillOrder);

        return order;
    }

    @Override
    public String createPath(User user, Long goodsId) {
        //生成秒杀路径
        String path = MD5Util.md5(UUIDUtil.uuid());
        //将值存入redis，并且设置超时时间60s
        //key:secKillPath:userId:goodsId
        redisTemplate.opsForValue().set("secKillPath:" + user.getId() + ":" + goodsId, path, 60, TimeUnit.SECONDS);
        return path;
    }

    @Override
    public boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId == null || goodsId < 0 || path == null || !StringUtils.isNotEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("secKillPath:" + user.getId() + ":" + goodsId);
        return redisPath.equals(path);
    }

    @Override
    public boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (user == null || goodsId == null || goodsId < 0 || captcha == null || !StringUtils.isNotEmpty(captcha)) {
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return redisCaptcha.equals(captcha);
    }
}
