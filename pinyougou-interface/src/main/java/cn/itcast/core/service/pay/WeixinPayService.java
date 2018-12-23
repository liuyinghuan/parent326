package cn.itcast.core.service.pay;


import java.util.HashMap;

public interface WeixinPayService {

    /**
     * 生成微信支付二维码
     * @param out_trade_no 订单id
     * @param total_fee 支付金额
     * @return (订单号,金额)
     */
    HashMap createNative(String out_trade_no,String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no 订单id
     * @return
     */
    HashMap queryPayStatus(String out_trade_no);


    /**
     * 循环查询支付状态
     * @param out_trade_no
     * @return
     */
    HashMap queryPayStatusWhile(String out_trade_no);
}
