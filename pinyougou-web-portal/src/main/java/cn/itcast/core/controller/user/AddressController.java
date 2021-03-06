package cn.itcast.core.controller.user;


import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.user.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Reference
    private AddressService addressService;

    /**
     * 查询用户地址
     * @param
     * @return
     */
    @RequestMapping("/findListByLoginUser.do")
    public List<Address> findListByLoginUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findListByLoginUser(username);
    }
}
