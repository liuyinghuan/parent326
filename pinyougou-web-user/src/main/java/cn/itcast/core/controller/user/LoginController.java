package cn.itcast.core.controller.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/name")
    public HashMap showName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        HashMap map=new HashMap();
        map.put("loginName",name);
        return map;
    }

}
