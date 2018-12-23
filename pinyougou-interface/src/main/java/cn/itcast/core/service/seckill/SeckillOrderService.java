package cn.itcast.core.service.seckill;

import cn.itcast.core.pojo.seckill.SeckillOrder;

public interface SeckillOrderService {


    /**
     * 提交秒杀订单
     * @param seckillId
     * @param userId
     */
    void submitOrder(Long seckillId,String userId);


    /**
     * 查询用户秒杀订单
     * @param userId
     * @return
     */
    SeckillOrder searchSeckillOrderFromRedis(String userId);


    /**
     * 客户支付完秒杀订单同步到数据库并且删除缓存
     * @param userId
     * @param orderId
     * @param transacationId
     */
    void saveOrderFromRedisToDb(String userId,Long orderId,String transacationId);
}
