package cn.itcast.core.service.pay;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.solr.common.util.Hash;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService{

    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;


    /**
     * 统一下单,生成微信二维码
     * @param out_trade_no 订单id
     * @param total_fee 支付金额
     * @return
     */

    @Override
    public HashMap createNative(String out_trade_no, String total_fee) {

        //构建参数,传递给微信
        HashMap map = new HashMap();
        map.put("appid",appid);//公众账号ID
        map.put("mch_id",partner);//商户号
        map.put("nonce_str", WXPayUtil.generateNonceStr());//微信提供的工具类能帮我们生成随机字符串
        //签名可以先不写,有个方法会直接自动加上签名
        map.put("body","品优购");//商品描述
        map.put("out_trade_no",out_trade_no);//商户订单号
        map.put("total_fee",total_fee);//支付的金额
        map.put("spbill_create_ip","127.0.0.1");//随便指定一个ip,用不到
        map.put("notify_url","http://pay.itcast.cn");//通知地址,也用不上
        map.put("trade_type","NATIVE");//交易类型 本地支付,也就是扫码支付

        try {
            //请求参数
            String parmaXml = WXPayUtil.generateSignedXml(map, partnerkey);//秘钥,会自动带上签名
            System.out.println("请求的参数:"+parmaXml);

            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);//是http请求
            httpClient.setXmlParam(parmaXml);//传递参数
            httpClient.post();//进行post提交

            //接收结果
            String resultXml = httpClient.getContent();
            System.out.println("返回的结果:"+resultXml);
            Map<String, String>  resultMap = WXPayUtil.xmlToMap(resultXml);


            HashMap m = new HashMap<>();//封装返回的结果
            //如果接口调用和返回成功
            if ("SUCCESS".equals(resultMap.get("return_code"))&&"SUCCESS".equals(resultMap.get("result_code"))){
                m.put("out_trade_no",out_trade_no);
                m.put("total_fee",total_fee);
                m.put("code_url",resultMap.get("code_url"));
            }else {
                System.out.println("出错");
            }
            //返回结果
            return m;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }



    }


    /**
     * 查询支付的状态
     * @param out_trade_no 订单id
     * @return
     */
    @Override
    public HashMap queryPayStatus(String out_trade_no) {

        //构建发送的参数
        HashMap map = new HashMap();
        map.put("appid",appid);//公众号id
        map.put("mch_id",partner);//商户号
        map.put("out_trade_no",out_trade_no);//订单号
        map.put("nonce_str",WXPayUtil.generateNonceStr());//获得随机字符串

        try {
            String parmaXml = WXPayUtil.generateSignedXml(map, partnerkey);//自动构建签名

            //发送请求
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(parmaXml);
            httpClient.post();

            //响应结果
            String resultXml = httpClient.getContent();
            System.out.println(resultXml);
            //封装返回的结果
            HashMap<String, String> resultMap = (HashMap<String, String>) WXPayUtil.xmlToMap(resultXml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 循环查询支付状态
     * @param out_trade_no
     * @return
     */
    @Override
    public HashMap queryPayStatusWhile(String out_trade_no) {

        int i=0;
        HashMap map = null;
        while (true){

            i++;
            //设置5分钟无响应,跳出循环
            if (i>=100){
                break;
            }
            //查询支付状态
            map = queryPayStatus(out_trade_no);

            if (map==null){
                break;
            }
            //支付成功调处循环
            if ("SUCCESS".equals(map.get("trade_state"))){
                break;
            }

            try {
                //每3秒查询一次
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
