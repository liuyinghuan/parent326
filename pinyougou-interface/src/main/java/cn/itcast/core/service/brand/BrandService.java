package cn.itcast.core.service.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有的品牌
     * @return
     */
    public List<Brand> findAll();

    /**
     * 品牌列表的分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(Integer pageNum, Integer pageSize);

    /**
     * 品牌列表的条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand);

    /**
     * 品牌保存
     * @param brand
     */
    public void add(Brand brand);

    /**
     * 查询对象的实体
     * @param id
     * @return
     */
    public Brand findOne(Long id);

    /**
     * 品牌更新
     * @param brand
     */
    public void update(Brand brand);

    /**
     * 品牌的批量删除
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 模板需要的品牌结果集
     */
    public List<Map<String, String>> selectOptionList();
}
