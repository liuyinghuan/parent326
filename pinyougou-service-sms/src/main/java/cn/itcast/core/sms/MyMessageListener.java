package cn.itcast.core.sms;

import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

public class MyMessageListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Value("${templateCode_smscode}")
    private String templateCode;

    @Value("${templateParam_smscode}")
    private String  param;


    @Override
    public void onMessage(Message message) {
        MapMessage  mapMessage=(MapMessage)message;
        try {
            String mobile = mapMessage.getString("mobile");
            String smscode = mapMessage.getString("smscode");
            System.out.println("接收到消息："+mobile+"  "+smscode);

            //完成发送
            smsUtil.sendSms( mobile,templateCode, param.replace("[value]",smscode));

        } catch (JMSException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }


    }
}
