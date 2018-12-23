package cn.itcast.core.controller.brand;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.brand.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService brandService;

    /**
     * 查询所有的品牌
     * @return
     */
    @RequestMapping("/findAll.do")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

    /**
     * 品牌列表的分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/findPage.do")
    public PageResult findPage(Integer pageNum, Integer pageSize){
        return brandService.findPage(pageNum, pageSize);
    }

    /**
     * 品牌列表的条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer pageNum, Integer pageSize, @RequestBody Brand brand){
        return brandService.search(pageNum, pageSize, brand);
    }

    /**
     * 新增品牌
     * @param brand
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody Brand brand){
        try{
            brandService.add(brand);
            return new Result(true, "保存成功");
        } catch (Exception e){
            e.printStackTrace();
            return new Result(false, "保存失败");
        }
    }

    /**
     * 查询对象的实体
     */
    @RequestMapping("/findOne.do")
    public Brand findOne(Long id){
        return brandService.findOne(id);
    }

    /**
     * 更新品牌
     * @param brand
     * @return
     */
    @RequestMapping("/update.do")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true, "更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    /**
     * 品牌批量删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 模板需要的品牌结果集
     * @return
     */
    @RequestMapping("/selectOptionList.do")
    public List<Map<String, String>> selectOptionList(){
        return brandService.selectOptionList();
    }

}
