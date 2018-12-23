package cn.itcast.core.service;


import cn.itcast.core.dao.seckill.SeckillGoodsDao;
import cn.itcast.core.dao.seckill.SeckillOrderDao;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.service.seckill.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService{


    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private SeckillGoodsDao seckillGoodsDao;

    @Resource
    private SeckillOrderDao seckillOrderDao;

    @Resource
    private IdWorker idWorker;

    /**
     * 提交秒杀订单
     * @param seckillId
     * @param userId
     */
    @Override
    public void submitOrder(Long seckillId, String userId) {

        //如果该用户已经存在秒杀订单,必须先完成该订单
        if (redisTemplate.boundHashOps("seckillOrder").get(userId)!=null){
            throw new RuntimeException("请先完成秒杀订单");
        }

        //从缓存中提取商品数据
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if (seckillGoods==null||seckillGoods.getNum()<=0){
            throw new RuntimeException("商品已被秒光");
        }

        //扣除缓存
        seckillGoods.setNum(seckillGoods.getNum()-1);
        seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
        //将跟新过的秒杀商品存入缓存
        redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);

        //如果秒杀的商品数量为0,将redis中的数据同步到数据库,并清除缓存
        if (seckillGoods.getNum()==0){
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            seckillGoodsDao.updateByPrimaryKey(seckillGoods);
        }

        //添加订单到缓存
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setStatus("0");
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setUserId(userId);
        redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);

    }


    /**
     * 查询到用户的秒杀订单
     * @param userId
     * @return
     */
    @Override
    public SeckillOrder searchSeckillOrderFromRedis(String userId) {

        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

        return seckillOrder;
    }


    /**
     * 用户支付完秒杀订单后保存数据库并且删除缓存
     * @param userId
     * @param orderId
     * @param transacationId
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transacationId) {
        SeckillOrder seckillOrder= (SeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder==null){
            throw new RuntimeException("订单不存在");
        }if (seckillOrder.getId().longValue()!=orderId){
            throw new RuntimeException("订单号不相符");
        }
        //否则保存到数据库并删除缓存
        //修改订单属性
        seckillOrder.setTransactionId(transacationId);
        seckillOrder.setStatus("1");//已支付
        seckillOrder.setPayTime(new Date());
        seckillOrderDao.insert(seckillOrder);

        //从缓存中删除
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }
}
