package cn.itcast.core.service.spec;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {

    /**
     * 规格列表查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    public PageResult search(Integer page, Integer rows, Specification specification);

    /**
     * 保存规格
     * @param specificationVo
     */
    public void add(SpecificationVo specificationVo);

    /**
     * 查询实体对象
     * @param id
     * @return
     */
    public SpecificationVo findOne(Long id);

    /**
     * 更新规格
     * @param specificationVo
     */
    public void update(SpecificationVo specificationVo);

    /**
     * 批量删除
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 模板需要的规格结果集
     * @return
     */
    List<Map<String,String>> selectOptionList();
}
