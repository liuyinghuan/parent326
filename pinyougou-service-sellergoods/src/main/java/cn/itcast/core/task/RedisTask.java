package cn.itcast.core.task;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private TypeTemplateDao typeTemplateDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    // 商品分类的数据同步到缓存中
    // 定义任务 cron：该程序执行的时间     秒分时日月年
    @Scheduled(cron="00 41 12 * * ?")
    public void autoItemCatsToRedis(){
        // 将商品分类的全部的数据放入到缓存中
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        if(itemCats != null && itemCats.size() > 0){
            for (ItemCat itemCat : itemCats) {
                // 使用redis的哪种数据结构-hash（散列）
                redisTemplate.boundHashOps("itemcat").put(itemCat.getName(), itemCat.getTypeId());
            }
            System.out.println("将商品分类同步到了redis中。。。");
        }
    }


    // 商品模板的数据同步到缓存中
    // 定义任务
    @Scheduled(cron="00 41 12 * * ?")
    public void autoTypeTemplatesToRedis(){
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
            System.out.println("将商品模板同步到了redis中。。。");
        }
    }

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
