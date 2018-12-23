package cn.itcast.core.service.spec;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.vo.SpecificationVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Resource
    private SpecificationDao specificationDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 规格列表查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        // 设置分页条件
        PageHelper.startPage(page, rows);
        // 设置查询条件
        SpecificationQuery specificationQuery = new SpecificationQuery();
        if(specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())){
            specificationQuery.createCriteria().andSpecNameLike("%"+specification.getSpecName().trim()+"%");
        }
        specificationQuery.setOrderByClause("id desc"); // 根据id降序
        // 根据条件查询
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
        // 将数据封装到PageResult中并返回
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 保存规格
     * @param specificationVo
     */
    @Transactional
    @Override
    public void add(SpecificationVo specificationVo) {
        // 保存规格
        Specification specification = specificationVo.getSpecification();
        specificationDao.insertSelective(specification); // 返回自增主键的id
        // 保存规格选项
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if(specificationOptionList != null && specificationOptionList.size() > 0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                // 设置外键
                specificationOption.setSpecId(specification.getId());
                // 插入
//                specificationOptionDao.insertSelective(specificationOption);
            }
            // 批量的插入
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    /**
     * 查询实体对象
     * @param id
     * @return
     */
    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();
        // 查询规格
        Specification specification = specificationDao.selectByPrimaryKey(id);
        specificationVo.setSpecification(specification);
        // 查询规格选项
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
        specificationVo.setSpecificationOptionList(specificationOptionList);
        return specificationVo;
    }

    /**
     * 更新规格
     * @param specificationVo
     */
    @Transactional
    @Override
    public void update(SpecificationVo specificationVo) {
        Specification specification = specificationVo.getSpecification();
        // 更新规格
        specificationDao.updateByPrimaryKeySelective(specification);
        // 更新规格选项
        // 先删除
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(specification.getId());
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        // 再添加
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if(specificationOptionList != null && specificationOptionList.size() > 0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                // 设置外键
                specificationOption.setSpecId(specification.getId());
            }
            // 批量的插入
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    /**
     * 批量删除
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length > 0){
            for (Long id : ids) {
                // 删除规格选项
                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
                specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
                specificationOptionDao.deleteByExample(specificationOptionQuery);
                // 删除规格
                specificationDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 模板需要的规格结果集
     * @return
     */
    @Override
    public List<Map<String, String>> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
