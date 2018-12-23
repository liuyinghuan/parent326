package cn.itcast.core.controller.pay;


import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.order.OrderService;
import cn.itcast.core.service.pay.WeixinPayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.IdWorker;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class WeixinPayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative.do")
    public HashMap creatNative(){
        //out_trade_no,total_fee不能靠前端传过来,否则安全性太低
        /*测试
        String out_trade_no= new IdWorker(0,0).nextId()+"";
        return weixinPayService.createNative(out_trade_no,"1");*/
        //从redis中获取订单的详情信息
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        PayLog payLog = orderService.searchPaylogFromRedis(userId);
        String outTradeNo = payLog.getOutTradeNo();
        Long totalFee = payLog.getTotalFee();
        return weixinPayService.createNative(outTradeNo,totalFee+"");
    }


    /**
     * 检查支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus.do")
    public Result queryPayStatus(String out_trade_no){
        HashMap map = weixinPayService.queryPayStatusWhile(out_trade_no);
        if (map==null){
            return new Result(false,"二维码超时");
        }else {
            if ("SUCCESS".equals(map.get("trade_state"))) {
                //支付成功更改订单状态
                orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));
                //返回结果
                Result result = new Result(true, "支付成功");
                return result;
            }else {
                return new Result(false,"支付失败");
            }
        }
    }
}
