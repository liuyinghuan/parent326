package cn.itcast.core.service.itemcat;

import cn.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {

    /**
     * 商品分类的列表查询
     * @param parentId
     * @return
     */
    public List<ItemCat> findByParentId(Long parentId);

    /**
     * 根据分类的id获取到模板id
     * @param id
     * @return
     */
    public ItemCat findOne(Long id);

    /**
     * 查询所有分类
     * @return
     */
    public List<ItemCat> findAll();
}
