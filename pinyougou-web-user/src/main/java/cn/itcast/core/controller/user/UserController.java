package cn.itcast.core.controller.user;

import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.user.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.PhoneFormatCheckUtils;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    @RequestMapping("/add")
    public Result add(@RequestBody User user,String smscode ){

        if(!userService.checkSmsCode(user.getPhone(),smscode)){
            return new Result( false,"验证码不正确");
        }
        try {
            userService.add(user);
            return new Result( true,"保存成功");
        }  catch (Exception e) {
            e.printStackTrace();
            return new Result( false,"保存失败");
        }
    }

    /**
     *  发送短信验证码
     * @param phone
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
            return new Result( false,"手机格式不合法");
        }

        try {
            userService.createSmsCode(phone);
            return new Result( true,"发送成功");
        }  catch (Exception e) {
            e.printStackTrace();
            return new Result( false,"发送失败");
        }
    }

}
