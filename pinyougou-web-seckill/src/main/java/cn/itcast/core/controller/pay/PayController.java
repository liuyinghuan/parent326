package cn.itcast.core.controller.pay;


import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.seckill.SeckillOrder;
import cn.itcast.core.service.pay.WeixinPayService;
import cn.itcast.core.service.seckill.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Reference;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;


    /**
     * 秒杀商品支付
     * @return
     */
    @RequestMapping("/createNative.do")
    public HashMap creatNative(){
        //取出用户名
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //取出秒杀商品订单
        SeckillOrder seckillOrder = seckillOrderService.searchSeckillOrderFromRedis(userId);
        if (seckillOrder!=null){
            long money_fen=(long)(seckillOrder.getMoney().doubleValue()*100);
            HashMap map = weixinPayService.createNative(seckillOrder.getId() + "", money_fen + "");
            return map;
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryOrderStatus(String out_trade_no){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = weixinPayService.queryPayStatusWhile(out_trade_no + "");
        if (map==null){
            return new Result(false,"支付失败");
        }else {
            if ("SUCCESS".equals(map.get("trade_state"))){
                //支付成功清空缓存,保存到数据库
                seckillOrderService.saveOrderFromRedisToDb(userId,Long.valueOf(out_trade_no),(String) map.get("transacation_id"));

                return new Result(true,"支付成功");
            }else {
                return new Result(false,"二维码超时");
            }
        }
    }

}
