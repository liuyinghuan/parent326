package cn.itcast.core.service.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.vo.GoodsVo;

public interface GoodsService {

    /**
     * 保存商品
     * @param goodsVo
     */
    public void add(GoodsVo goodsVo);

    /**
     * 查询当前商家的商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    public PageResult search(Integer page, Integer rows, Goods goods);

    /**
     * 回显商品数据
     * @param id
     * @return
     */
    public GoodsVo findOne(Long id);

    /**
     * 商品的更新
     * @param goodsVo
     */
    public void update(GoodsVo goodsVo);

    /**
     * 运营商查询待审核的商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    public PageResult searchForManager(Integer page, Integer rows, Goods goods);

    /**
     * 商品的审核
     * @param ids
     * @param status
     */
    public void updateStatus(Long[] ids, String status);

    /**
     * 删除商品
     * @param ids
     */
    public void delete(Long[] ids);
}
