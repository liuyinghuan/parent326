package cn.itcast.core.controller.goods;

import cn.itcast.core.entity.PageResult;
import cn.itcast.core.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.goods.GoodsService;
import cn.itcast.core.service.page.ItemPageService;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;


    /**
     * 运营商查询待审核的商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){
        return goodsService.searchForManager(page, rows, goods);
    }

    /**
     * 审核商品
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids, String status){
        try {
            goodsService.updateStatus(ids, status);

            for(Long id:ids){
                itemPageService.genHtml(id);
            }

            return new Result(true, "操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "操作失败");
        }
    }

    /**
     * 删除商品
     * @param ids
     * @return
     */
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "操作失败");
        }
    }


    @Reference
    private ItemPageService itemPageService;

    @RequestMapping("/genHtml")
    public void genHtml(Long goodsId){
        itemPageService.genHtml(goodsId);
    }

}
