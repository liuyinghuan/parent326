package cn.itcast.core.service.user;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Destination;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService{


    @Autowired
    private UserDao userDao;


    @Override
    public void add(User user) {
        user.setCreated(new Date());
        user.setUpdated(new Date());
        //密码加密
        String newpassword = DigestUtils.md5Hex(user.getPassword());
        user.setPassword(newpassword);
        userDao.insert(user);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination smscodeDestination;

    @Override
    public void createSmsCode(String phone) {

        //1.生成一个短信验证码  6位数字   7
        long code= (long)(Math.random()*1000000);
        if(code<100000){
            code=code+100000;
        }
        System.out.println("验证码："+code);
        //2.存入redis
        redisTemplate.boundValueOps("smscode_"+phone ).set(code+"",5, TimeUnit.MINUTES);
        //3.发送到消息队列
        HashMap map=new HashMap();
        map.put("mobile",phone);
        map.put("smscode",code+"");
        jmsTemplate.convertAndSend(smscodeDestination,map);
    }

    /**
     * 验证短信验证码
     * @param phone
     * @param smscode
     * @return
     */
    @Override
    public boolean checkSmsCode(String phone,String smscode){
        //取出系统的验证码
        String  syscode =(String)redisTemplate.boundValueOps("smscode_" + phone).get();

        if(smscode.equals(syscode) && syscode!=null){
            return true;
        }else{
            return false;
        }
    }

}
