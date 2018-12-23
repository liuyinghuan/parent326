package cn.itcast.core.service.order;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojo.order.OrderQuery;
import cn.itcast.core.vo.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private IdWorker idWorker;

    @Resource
    private OrderItemDao orderItemDao;

    @Resource
    private PayLogDao payLogDao;//支付日志

    /**
     * 保存订单
     * @param order
     */
    @Transactional
    @Override
    public void add(Order order) {
        //取出redis中的购物车
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        if (cartList==null){
            return;
        }
        //订单号生成
        String outTradeNo= idWorker.nextId()+"";// 支付订单号

        long total_money=0;//所有订单的总金额

        //循环添加订单
        for (Cart cart : cartList) {
            Order tbOrder = new Order();
            //订单id需要生成
            long orderId = idWorker.nextId();
            tbOrder.setOrderId(orderId);
            tbOrder.setStatus("1");//未付款
            tbOrder.setPaymentType(order.getPaymentType());//支付的方式
            tbOrder.setCreateTime(new Date());// 创建日期
            tbOrder.setUserId(order.getUserId());//用户ID
            tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
            tbOrder.setReceiverMobile(order.getReceiverMobile());//电话
            tbOrder.setReceiver(order.getReceiver());//收货人
            tbOrder.setSourceType(order.getSourceType());//订单来源
            tbOrder.setSellerId(cart.getSellerId());//商家ID
            tbOrder.setOutTradeNo(outTradeNo);//支付订单号

            double money=0;//金额
            //添加订单明细到数据库
            for (OrderItem orderItem:cart.getOrderItemList()){
                orderItem.setOrderId(orderId);//订单id
                orderItem.setId(idWorker.nextId());//订单明细id
                orderItemDao.insert(orderItem);
                //计算单个订单的总金额
                money+=orderItem.getTotalFee().doubleValue();//金额累加
            }
            //整个订单的总金额
            total_money+=(long)(money*100) ;
            BigDecimal bigDecimal = new BigDecimal(money);
            tbOrder.setPayment(bigDecimal);//金额

            orderDao.insert(tbOrder);
        }

        //判断如果是微信支付，在支付日志中添加记录
        if("1".equals(order.getPaymentType() )){
            PayLog payLog=new PayLog();//支付日志=支付订单
            payLog.setOutTradeNo( outTradeNo );
            payLog.setCreateTime(new Date());
            payLog.setTotalFee( total_money );//总金额
            payLog.setUserId(order.getUserId());
            payLog.setTradeState("0");// 0 未支付

            //将日志保存到redis中
            redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

            payLogDao.insert(payLog);
        }

        //清除redis中购物车的记录
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());


    }

    /**
     * 获取日志中订单的详情信息
     * @param userId
     * @return
     */
    @Override
    public PayLog searchPaylogFromRedis(String userId) {
        return (PayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    /**
     * 修改订单状态
     * @param out_trade_no
     * @param transaction_id
     */
    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //更改支付日志的订单状态
        PayLog payLog = payLogDao.selectByPrimaryKey(out_trade_no);
        payLog.setTransactionId(transaction_id);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");//将支付状态改为已支付
        payLogDao.updateByPrimaryKey(payLog);

        //更改订单状态
        Order order = new Order();
        order.setStatus("2");

        OrderQuery orderQuery=new OrderQuery();
        OrderQuery.Criteria criteria = orderQuery.createCriteria();
        criteria.andOutTradeNoEqualTo(out_trade_no);
        orderDao.updateByExampleSelective(order,orderQuery);

        //清除redis中的日志缓存
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}
