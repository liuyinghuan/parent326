package cn.itcast.core.service;


import cn.itcast.core.dao.seckill.SeckillGoodsDao;
import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.pojo.seckill.SeckillGoodsQuery;
import cn.itcast.core.service.seckill.SeckillGoodsService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class SeckillGoodsImpl implements SeckillGoodsService{

    @Resource
    private SeckillGoodsDao seckillGoodsDao;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 查询所有符合条件的秒杀商品
     * @return
     */
    @Override
    public List<SeckillGoods> findList() {
        //首先查询缓存中的数据
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();

        if(seckillGoodsList==null || seckillGoodsList.size()==0) {//如果缓存中没有数据
            System.out.println("从数据库中取出商品记录");
            //封装查询条件
            SeckillGoodsQuery seckillGoodsQuery = new SeckillGoodsQuery();
            SeckillGoodsQuery.Criteria criteria = seckillGoodsQuery.createCriteria();
            //库存数量大于0
            criteria.andNumGreaterThan(0);
            //商品状态必须以审核
            criteria.andStatusEqualTo("1");
            //大于或者等于当前日期
            criteria.andStartTimeLessThanOrEqualTo(new Date());
            //当前日期小于结束日期
            criteria.andEndTimeGreaterThan(new Date());
            seckillGoodsList = seckillGoodsDao.selectByExample(seckillGoodsQuery);

            //将秒杀商品列表放入缓存
            for( SeckillGoods seckillGoods:seckillGoodsList ){
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            }
        }else {
            System.out.println("从缓存中取出数据");
        }
        return seckillGoodsList;
    }


    /**
     * 从redis中根据id查出单个秒杀商品
     * @param id
     * @return
     */
    @Override
    public SeckillGoods findOne(Long id) {
       return (SeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);

    }
}
