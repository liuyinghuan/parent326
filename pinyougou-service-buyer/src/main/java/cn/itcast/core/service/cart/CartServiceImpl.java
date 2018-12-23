package cn.itcast.core.service.cart;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.vo.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Resource
    private ItemDao itemDao;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 将商品添加到购物车
     * @param cartList 元购物车列表
     * @param itemId 需要添加的商品id
     * @param num 添加的商品数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据商品id查询壶商品对象
        Item item = itemDao.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("商品部存在");
        }
        if (!"1".equals(item.getStatus())){
            throw new RuntimeException("商品不存在");
        }
        //2.根据商品对象获取到商家id
        String sellerId = item.getSellerId();
        //3.判断集合中是否已经存在该商家
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果不存在
        if (cart==null){
            //4.1创建该商家的购物车
            cart =new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());//商家名称
            //4.1创建购物车明细对象
            OrderItem orderItem = createOrderItem(item, num);
            //将商品明细加入到购物车
            List<OrderItem> orderItemList=new ArrayList<>();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {
            //5如果集合中已经存在该商家
            //5.1判断该商家购物车是否有该商品明细对象
            OrderItem orderItem = searchOrderItemByitemId(cart.getOrderItemList(), itemId);
            //5.2如果购物车没有该商品明细对象,创建该商品的明细对象
            if (orderItem==null){
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }else {
                //5.3如果购物车已经存在了,则将该明细对象数量加上添加的数量
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum()));
                //如果该明细列表数量小于0,则将该明细移除
                if (orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }
                //如果该购物车明细数量为0,移除该购物车
                if (cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }
            }

        }
        return cartList;
    }


    /**
     * 根据用户名将购物车从redis中取出
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车:"+username);
        List<Cart> cartList= (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            return new ArrayList<>();
        }
        return cartList;
    }


    /**
     * 将购物车存入到redis中
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存入username:"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }


    /**
     * 合并本地的购物车和redis中的购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        //任意将其中一个购物车循环取出商品添加到另一个购物车中
        for (Cart cart : cartList1) {
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //添加商品合并到cartList2中
                cartList2 = addGoodsToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        //将合并后得购物车返回
        return cartList2;
    }



    /**
     * 判断是否存在该商品明细
     * @param orderItemList
     * @param itemId
     * @return
     */
    private OrderItem searchOrderItemByitemId(List<OrderItem> orderItemList,Long itemId){
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }



    /**
     * 创建商品明细对象
     * @param item
     * @param num
     * @return
     */
    private OrderItem createOrderItem(Item item,Integer num){
        if (num==0){
            num=1;
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }



    /**
     * 判断购物车是否存在该商家
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
}
