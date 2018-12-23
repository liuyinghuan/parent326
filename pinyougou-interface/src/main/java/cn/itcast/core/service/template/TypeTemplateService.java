package cn.itcast.core.service.template;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService {

    /**
     * 模板列表查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);

    /**
     * 模板保存
     * @param typeTemplate
     */
    public void add(TypeTemplate typeTemplate);

    /**
     * 确定了模板后加载的品牌列表以及扩展属性列表
     * @param id
     * @return
     */
    public TypeTemplate findOne(Long id);

    /**
     * 确定了模板后加载的规格、以及规格选项列表
     * @param id
     * @return
     */
    public List<Map> findBySpecList(Long id);
}
