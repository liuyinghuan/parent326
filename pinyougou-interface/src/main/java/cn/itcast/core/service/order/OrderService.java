package cn.itcast.core.service.order;

import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

public interface OrderService {

    /**
     * 订单业务接口
     */

    public void add(Order order);

    /**
     * 获取redis中的日志对象
     * @param userId
     * @return 日志中提交订单的详情信息
     */
    PayLog searchPaylogFromRedis(String userId);

    /**
     * 修改订单状态
     * @param out_trade_no
     * @param transaction_id
     */
    void updateOrderStatus(String out_trade_no,String transaction_id);
}
