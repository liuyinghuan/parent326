package cn.itcast.core.service.content;

import java.util.List;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.ad.ContentQuery;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private ContentDao contentDao;

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
	    // 清除缓存
        clearCache(content.getCategoryId());
		contentDao.insertSelective(content);
	}

    private void clearCache(Long categoryId) {
	    redisTemplate.boundHashOps("content").delete(categoryId);
    }

    @Override
	public void edit(Content content) {
        // 清除缓存
        // 判断广告的分类是否发生改变，不改变：删除  改变：本次和之前的数据都需要清空
        Long newCategoryId = content.getCategoryId();
        Long oldCategoryId = contentDao.selectByPrimaryKey(content.getId()).getCategoryId(); // 在更新之前查询出来
        if(newCategoryId != oldCategoryId){
            // 分类改变了，全部清空
            clearCache(newCategoryId);
            clearCache(oldCategoryId);
        }else{
            clearCache(oldCategoryId);
        }
        contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
                // 清除缓存
                Content content = contentDao.selectByPrimaryKey(id);
                clearCache(content.getCategoryId());
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 查询该分类下的广告列表
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
	    // 从缓存中获取数据
        List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		// 判断缓存中是否存在：不存在，
        if(list == null){
            synchronized (this){
                list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
                if(list == null){
                    // 从数据库中查询
                    ContentQuery contentQuery = new ContentQuery();
                    contentQuery.createCriteria().andCategoryIdEqualTo(categoryId);
                    list = contentDao.selectByExample(contentQuery);
                    // 并且放入缓存
                    redisTemplate.boundHashOps("content").put(categoryId, list);
                }
            }
        }
		return list;
	}
}
