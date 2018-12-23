package cn.itcast.core.controller.cart;

import cn.itcast.core.entity.LoginResult;
import cn.itcast.core.service.cart.CartService;
import cn.itcast.core.vo.Cart;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @RequestMapping("/addGoodsToCartList.do")
    public LoginResult addGoodsToCartList(@RequestBody List<Cart> cartList, Long itemId, Integer num){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("用户名是:::::::::::::::::::"+username);
        if ("anonymousUser".equals(username)){
            username="";
        }
        try {
            List<Cart> carts = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("".equals(username)){

                return new LoginResult(true,"",carts);
            }else {
                //和redis中的合并
                List<Cart> cartList_Redis = cartService.findCartListFromRedis(username);
                cartList_Redis = cartService.mergeCartList(carts, cartList_Redis);
                cartService.saveCartListToRedis(username,cartList_Redis);
                return new LoginResult(true,username,cartList_Redis);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResult(false,"","添加失败");
        }

    }


    /**
     * 查询购物车
     * @param cartList
     * @return
     */
    @RequestMapping("/findCartList.do")
    public LoginResult findCartList(@RequestBody List<Cart> cartList){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("用户名是:::::::::::::::::::"+username);
        //判断当前用户名是否登录
        if ("anonymousUser".equals(username)){
            //未登录,直接将购物车返回
            return new LoginResult(true,"",cartList);
        }else {
            //已登录,将本地和redis里面的购物车合并
            //取出redis中的购物车
            List<Cart> cartList_Redis = cartService.findCartListFromRedis(username);
            //如果本地购物车有商品合并购物车
            if (cartList.size()>0){
                cartList_Redis = cartService.mergeCartList(cartList, cartList_Redis);
            }
            //将合并后得购物车保存到redis
            cartService.saveCartListToRedis(username,cartList_Redis);
            return new LoginResult(true,username,cartList_Redis);
        }
    }
}
