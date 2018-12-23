package cn.itcast.core.service.search;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Resource
    private SolrTemplate solrTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 前台系统检索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        // 封装所有的结果集
        Map<String, Object> resultMap = new HashMap<>();
        // 处理输入的关键字的内容中包含的空格
        String keywords = searchMap.get("keywords");
        if(keywords != null && !"".equals(keywords)){
            keywords = keywords.replace(" ", "");
            searchMap.put("keywords", keywords);
        }
        // 根据关键字检索并且进行分页
//        Map<String, Object> map = searchForPage(searchMap);
        // 根据关键字检索并且分页,并且关键字高亮
        Map<String, Object> map = searchForHighLightPage(searchMap);
        resultMap.putAll(map);
        // 根据关键字查询商品的分类：分组
        List<String> categoryList = searchForGroupPage(searchMap);
        // 默认加载第一个分类下的品牌以及规格
        if(categoryList != null && categoryList.size() > 0){
            Map<String, Object> brandAndSepcMap = searchBrandListAndSpecListForCatagroy1(categoryList.get(0));
            resultMap.putAll(brandAndSepcMap);

            resultMap.put("categoryList", categoryList);
        }


        return resultMap;
    }

    // 默认加载第一个分类下的品牌以及规格
    private Map<String,Object> searchBrandListAndSpecListForCatagroy1(String name) {
        // 根据分类获取模板id
        Object typeId = redisTemplate.boundHashOps("itemcat").get(name);
        // 获取品牌结果集
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        // 获取规格结果集
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        // 封装结果
        Map<String,Object> map = new HashMap<>();
        map.put("brandList", brandList);
        map.put("specList", specList);
        return map;
    }

    // 查询商品的分类
    private List<String> searchForGroupPage(Map<String, String> searchMap) {
        // 封装检索的条件
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords");
        if(keywords != null && !"".equals(keywords)){
            criteria.is(keywords); // is:模糊查询
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 设置分组条件
        GroupOptions groupOptions = new GroupOptions();
        groupOptions.addGroupByField("item_category"); // 设置分组的字段
        query.setGroupOptions(groupOptions);

        // 根据条件查询
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);

        // 处理结果集
        List<String> categoryList = new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry : groupEntries) {
            String groupValue = groupEntry.getGroupValue();
            // 将分类的名称放入集合中
            categoryList.add(groupValue);
        }
        return categoryList;
    }

    // 根据关键字检索并且分页,并且关键字高亮
    private Map<String,Object> searchForHighLightPage(Map<String, String> searchMap) {
        // 设置检索条件
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords");
        if(keywords != null && !"".equals(keywords)){
            // 封装检索的条件
            criteria.is(keywords); // is:模糊查询
        }
        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);
        // 设置分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);  // 其始行
        query.setRows(pageSize);    // 每页显示的条数
        // 设置关键字高亮:对检索的内容添加HTML的标签
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title"); // 需要对哪个字段进行高亮
        highlightOptions.setSimplePrefix("<font color='red'>"); // 开始标签
        highlightOptions.setSimplePostfix("</font>");           // 结束标签
        query.setHighlightOptions(highlightOptions);    // 设置高亮操作

        // 添加过滤的条件
        // 商品分类
        String category = searchMap.get("category");
        if(category != null && !"".equals(category)){
            Criteria cri = new Criteria("item_category");
            cri.is(category);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 商品品牌
        String brand = searchMap.get("brand");
        if(brand != null && !"".equals(brand)){
            Criteria cri = new Criteria("item_brand");
            cri.is(brand);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        // 商品规格:{"网络":"4G","机身内存":"32G"}
        String spec = searchMap.get("spec");
        if(spec != null && !"".equals(spec)){
            Map<String, String> map = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria cri = new Criteria("item_spec_"+entry.getKey());
                cri.is(entry.getValue());
                SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }
        // 商品价格
        String price = searchMap.get("price");
        if(price != null && !"".equals(price)){
            String[] prices = price.split("-");
            Criteria cri = new Criteria("item_price");
            cri.between(prices[0], prices[1], true, true);
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }

        // 结果排序：新品、价格
        // 根据新品排序：sortField，排序字段   sort：排序规则
        String s = searchMap.get("sort");
        if(s != null && !"".equals(s)){
            if("ASC".equals(s)){
                Sort sort = new Sort(Sort.Direction.ASC, "item_"+searchMap.get("sortField"));
                query.addSort(sort);
            }else{
                Sort sort = new Sort(Sort.Direction.DESC, "item_"+searchMap.get("sortField"));
                query.addSort(sort);
            }

        }



        // 根据条件查询
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);
        // 处理高亮的结果
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if(highlighted != null && highlighted.size() > 0){
            for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
                Item item = itemHighlightEntry.getEntity(); // 普通的结果
                List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights(); // 高亮的结果
                if(highlights != null && highlights.size() > 0){
                    for (HighlightEntry.Highlight highlight : highlights) {
                        String title = highlight.getSnipplets().get(0);// 高亮的结果
                        item.setTitle(title);
                    }
                }
            }
        }
        // 处理结果集
        Map<String,Object> map = new HashMap<>();
        map.put("totalPages", highlightPage.getTotalPages());  // 总页数
        map.put("total", highlightPage.getTotalElements());    // 总条数
        map.put("rows", highlightPage.getContent());           // 结果集
        return map;
    }

    // 根据关键字检索并且进行分页
    private Map<String,Object> searchForPage(Map<String, String> searchMap) {
        // 创建query并且封装查询条件
        String keywords = searchMap.get("keywords");
        Criteria criteria = new Criteria("item_keywords");
        if(keywords != null && !"".equals(keywords)){
            // 封装检索的条件
            criteria.is(keywords); // is:模糊查询
        }
        SimpleQuery query = new SimpleQuery(criteria);
        // 设置分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));
        Integer offset = (pageNo - 1) * pageSize;
        query.setOffset(offset);  // 其始行
        query.setRows(pageSize);    // 每页显示的条数

        // 根据条件查询
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(query, Item.class);

        // 处理结果集
        Map<String,Object> map = new HashMap<>();
        map.put("totalPages", scoredPage.getTotalPages());  // 总页数
        map.put("total", scoredPage.getTotalElements());    // 总条数
        map.put("rows", scoredPage.getContent());           // 结果集
        return map;
    }
}
