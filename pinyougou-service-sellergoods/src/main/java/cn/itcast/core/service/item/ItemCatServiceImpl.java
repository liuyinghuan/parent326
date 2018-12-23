package cn.itcast.core.service.item;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import cn.itcast.core.service.itemcat.ItemCatService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ItemCatServiceImpl implements ItemCatService {

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 商品分类的列表查询
     * @param parentId
     * @return
     */
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        // 将商品分类的全部的数据放入到缓存中
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        if(itemCats != null && itemCats.size() > 0){
            for (ItemCat itemCat : itemCats) {
                // 使用redis的哪种数据结构-hash（散列）
                redisTemplate.boundHashOps("itemcat").put(itemCat.getName(), itemCat.getTypeId());
            }
        }

        // 设置查询条件
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        itemCatQuery.createCriteria().andParentIdEqualTo(parentId);
        return itemCatDao.selectByExample(itemCatQuery);
    }

    /**
     * 根据分类的id获取到模板id
     * @param id
     * @return
     */
    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    /**
     * 查询所有分类
     * @return
     */
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}
