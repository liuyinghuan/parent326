package cn.itcast.core.service.brand;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    // 好处：
    // 1、jdk提供注解 效率高
    // 2、降低耦合度
    @Resource
    private BrandDao brandDao;


    /**
     * 查询所有品牌
     * @return
     */
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    /**
     * 品牌的分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        // 自己实现：需要计算出其始行  项目：通过分页助手去实现
        // 设置分页条件
        PageHelper.startPage(pageNum, pageSize);
        // 查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        // 将数据封装到PageResult中
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    /**
     * 品牌列表的条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        // 设置分页条件
        PageHelper.startPage(pageNum, pageSize);
        // 设置查询条件
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        // 封装品牌的名称
        if(brand.getName() != null && !"".equals(brand.getName().trim())){
            // 条件封装：拼接sql语句，没有%
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }
        if(brand.getFirstChar() != null && !"".equals(brand.getFirstChar().trim())){
            // 条件封装：拼接sql语句
            criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
        }
        // 根据id降序
        brandQuery.setOrderByClause("id desc");
        // 根据条件查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 品牌保存
     * @param brand
     */
    @Transactional
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    /**
     * 查询对象的实体
     * @param id
     * @return
     */
    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    /**
     * 品牌更新
     * @param brand
     */
    @Transactional
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 品牌的批量删除
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length > 0){
//            for (Long id : ids) {
//                brandDao.deleteByPrimaryKey(id);
//            }
            // 缺点：频繁的连接数据库、提交事务  效率低
            brandDao.deleteByPrimaryKeys(ids);
        }

    }

    /**
     * 模板需要的品牌结果集
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }
}
