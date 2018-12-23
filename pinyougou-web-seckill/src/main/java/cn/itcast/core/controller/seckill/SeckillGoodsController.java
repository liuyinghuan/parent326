package cn.itcast.core.controller.seckill;


import cn.itcast.core.pojo.seckill.SeckillGoods;
import cn.itcast.core.service.seckill.SeckillGoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController {

    @Reference(timeout = 10000)
    private SeckillGoodsService seckillGoodsService;

    /**
     * 查询所有秒杀商品
     * @return
     */
    @RequestMapping("/findList.do")
    public List<SeckillGoods> findList(){
        return seckillGoodsService.findList();
    }

    /**
     * 根据id查询单个秒杀商品
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public SeckillGoods findOne(Long id){
        return seckillGoodsService.findOne(id);
    }
}
