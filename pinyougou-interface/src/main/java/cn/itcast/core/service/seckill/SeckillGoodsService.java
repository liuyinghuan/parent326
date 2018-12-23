package cn.itcast.core.service.seckill;

import cn.itcast.core.pojo.seckill.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    /**
     * 查询全部符合条件的秒杀商品
     * @return
     */
    List<SeckillGoods> findList();


    SeckillGoods findOne(Long id);
}
