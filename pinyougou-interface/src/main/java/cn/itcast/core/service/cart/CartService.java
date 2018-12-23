package cn.itcast.core.service.cart;

import cn.itcast.core.vo.Cart;

import java.util.List;

/**
 * 购物车接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     * @param cartList 元购物车列表
     * @param itemId 需要添加的商品id
     * @param num 添加的商品数量
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

    /**
     * 根据用户名取出购物车
     * @param username
     * @return
     */
    List<Cart> findCartListFromRedis(String username);


    /**
     * 将购物车存入到redis
     * @param username
     * @param cartList
     */
     void saveCartListToRedis(String username,List<Cart> cartList);


    /**
     * 合并本地和redis中的购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
     List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
