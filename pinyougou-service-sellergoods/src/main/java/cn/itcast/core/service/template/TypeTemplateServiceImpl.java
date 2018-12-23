package cn.itcast.core.service.template;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 模板的列表查询
     * @param page
     * @param rows
     * @param typeTemplate
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {

        // 将模板中的品牌以及规格结果集放入缓存中
        List<TypeTemplate> list = typeTemplateDao.selectByExample(null);
        if(list != null && list.size() > 0){
            for (TypeTemplate template : list) {
                // 品牌结果集
                String brandIds = template.getBrandIds();
                List<Map> brandList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(), brandList);
                // 规格选项结果集
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(), specList);

            }
        }

        // 设置分页条件
        PageHelper.startPage(page, rows);
        // 设置查询条件
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        if(typeTemplate.getName() != null && !"".equals(typeTemplate.getName().trim())){
            typeTemplateQuery.createCriteria().andNameLike("%"+typeTemplate.getName().trim()+"%");
        }
        PageHelper.orderBy("id desc"); // 本质：都是在拼接sql语句的条件
        // 根据条件查询
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);
        // 将结果封装到PageResult中并返回
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 模板保存
     * @param typeTemplate
     */
    @Transactional
    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    /**
     * 确定了模板后加载的品牌列表以及扩展属性列表
     * @param id
     * @return
     */
    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    /**
     * 确定了模板后加载的规格、以及规格选项列表
     * @param id
     * @return
     */
    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        // 栗子：[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        String specIds = typeTemplate.getSpecIds();
        // 将json串转成对象
        List<Map> list = JSON.parseArray(specIds, Map.class);
        // 根据规格获取到规格选项
        for (Map map : list) {
            Long specId = Long.parseLong(map.get("id").toString()); // 规格id
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            query.createCriteria().andSpecIdEqualTo(specId);
            List<SpecificationOption> options = specificationOptionDao.selectByExample(query);
            // [{"id":27,"text":"网络","options":[{},{}...]},{"id":32,"text":"机身内存"}]
            map.put("options", options);
        }
        return list;
    }
}
