package cn.itcast.core.controller.seckill;


import cn.itcast.core.entity.Result;
import cn.itcast.core.service.seckill.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;


    /**
     * 提交秒杀订单
     * @param seckillId
     */
    @RequestMapping("/submitOrder.do")
    public Result submitOrder(Long seckillId){

        //取出用户id
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //用户未登录
        if ("anonymousUser".equals(userId)){
            return new Result(false,"请先登录");
        }

        try {
            seckillOrderService.submitOrder(seckillId,userId);
            return new Result(true,"订单提交成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "订单体检失败");
        }
    }
}
