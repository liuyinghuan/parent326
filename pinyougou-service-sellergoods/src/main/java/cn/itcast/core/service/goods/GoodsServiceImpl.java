package cn.itcast.core.service.goods;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private SolrTemplate solrTemplate;

    /**
     * 保存商品
     * @param goodsVo
     */
    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
        // 1、保存商品信息
        Goods goods = goodsVo.getGoods();
        goods.setAuditStatus("0"); // 默认商品待审核的状态
        goodsDao.insertSelective(goods);
        // 2、保存商品描述信息
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescDao.insertSelective(goodsDesc);
        // 3、保存商品对应的库存信息
        // 判断是否启用规格
        if("1".equals(goods.getIsEnableSpec())){
            // 启用规格：一个商品对应多个库存
            List<Item> itemList = goodsVo.getItemList();
            if(itemList != null && itemList.size() > 0){
                for (Item item : itemList) {
                    // 商品标题：spu名称+spu的副标题+规格名称
                    String title = goods.getGoodsName() + " " + goods.getCaption();
                    // 规格的数据：{"机身内存":"16G","网络":"联通3G"}
                    String spec = item.getSpec();
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for(Map.Entry<String, String> entry : entries){
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    setAttributeForItem(goods, goodsDesc, item); // 设置库存表的属性
                    itemDao.insertSelective(item);
                }
            }
        }else{
            // 不启用规格：一个商品对应一个库存
            Item item = new Item();
            item.setTitle(goods.getGoodsName() + " " + goods.getCaption());
            item.setPrice(goods.getPrice());
            item.setNum(9999);
            item.setIsDefault("1");
            item.setSpec("{}");
            setAttributeForItem(goods, goodsDesc, item); // 设置库存表的属性
            itemDao.insertSelective(item);
        }
    }

    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        // 商品图片
        // [{"color":"白色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVnGZfWAaX2hAAjlKdWCzvg173.jpg"},
        // {"color":"白色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVnGZfWAaX2hAAjlKdWCzvg173.jpg"}]
        String itemImages = goodsDesc.getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if(images != null && images.size() > 0){
            // 在检索的过程中：用于显示
            String image = images.get(0).get("url").toString();
            item.setImage(image);
        }
        item.setCategoryid(goods.getCategory3Id()); // 三级分类的id
        item.setStatus("1"); // 商品状态
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goods.getId());
        item.setSellerId(goods.getSellerId());
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName()); // 分类名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());    // 品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());   // 商家店铺名称
    }

    /**
     * 查询当前商家的商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        // 设置分页条件
        PageHelper.startPage(page, rows);
        // 设置查询条件:根据商家的id查询
        GoodsQuery goodsQuery = new GoodsQuery();
        if(goods.getSellerId() != null && !"".equals(goods.getSellerId().trim())){
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId().trim());
        }
        goodsQuery.setOrderByClause("id desc");
        // 查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 回显商品数据
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo = new GoodsVo();
        // 商品信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
        // 商品描述信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
        // 商品对应的库存信息
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(itemList);
        return goodsVo;
    }

    /**
     * 商品的更新
     * @param goodsVo
     */
    @Transactional
    @Override
    public void update(GoodsVo goodsVo) {
        // 更新商品
        Goods goods = goodsVo.getGoods();
        // 更改后的商品需要重新进行提交审核，因此初始化审核状态
        goods.setAuditStatus("0");
        goodsDao.updateByPrimaryKeySelective(goods);
        // 更新商品描述
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        // 更新商品对应的库存：
        // 先删除
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);
        // 再插入
        // 判断是否启用规格
        if("1".equals(goods.getIsEnableSpec())){
            // 启用规格：一个商品对应多个库存
            List<Item> itemList = goodsVo.getItemList();
            if(itemList != null && itemList.size() > 0){
                for (Item item : itemList) {
                    // 商品标题：spu名称+spu的副标题+规格名称
                    String title = goods.getGoodsName() + " " + goods.getCaption();
                    // 规格的数据：{"机身内存":"16G","网络":"联通3G"}
                    String spec = item.getSpec();
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for(Map.Entry<String, String> entry : entries){
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    setAttributeForItem(goods, goodsDesc, item); // 设置库存表的属性
                    itemDao.insertSelective(item);
                }
            }
        }else{
            // 不启用规格：一个商品对应一个库存
            Item item = new Item();
            item.setTitle(goods.getGoodsName() + " " + goods.getCaption());
            item.setPrice(goods.getPrice());
            item.setNum(9999);
            item.setIsDefault("1");
            item.setSpec("{}");
            setAttributeForItem(goods, goodsDesc, item); // 设置库存表的属性
            itemDao.insertSelective(item);
        }
    }

    /**
     * 运营商查询待审核的商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult searchForManager(Integer page, Integer rows, Goods goods) {
        // 设置分页条件
        PageHelper.startPage(page, rows);
        // 设置查询条件: 待审核并且未删除
        GoodsQuery goodsQuery = new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        if(goods.getAuditStatus() != null && !"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        criteria.andIsDeleteIsNull(); // 未删除
        goodsQuery.setOrderByClause("id desc");
        // 查询
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(), p.getResult());
    }

    /**
     * 商品的审核
     * @param ids
     * @param status
     */
    @Transactional
    @Override
    public void updateStatus(Long[] ids, String status) {
        if(ids != null && ids.length > 0){
            // 更新商品的审核状态
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                // 审核成功后需要处理的业务
                if("1".equals(status)){
                    // TODO 将商品信息保存到索引库中
                    // 合理业务：将审核通过后的商品保存到索引库
                    // 今天：为了明天的搜索，将全部数据保存到索引库
                    dataImportItemToSolr();
                    // TODO 生成该商品详情的静态页
                }
            }
        }

    }

    // 将数据库的数据保存索引库中
    private void dataImportItemToSolr() {
        List<Item> items = itemDao.selectByExample(null);
        if(items != null && items.size() > 0){
            for (Item item : items) {
                String spec = item.getSpec();
                Map specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }
    }

    /**
     * 删除商品
     * @param ids
     */
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length > 0){
            Goods goods = new Goods();
            goods.setIsDelete("1"); // 1：代表删除
            for (Long id : ids) {
                // 更新删除状态
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                // TODO 删除索引库中的数据
            }
        }
    }
}
