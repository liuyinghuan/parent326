package cn.itcast.core.service.search;

import java.util.Map;

public interface ItemSearchService {

    /**
     * 前台系统检索
     * @param searchMap
     * @return
     */
    public Map<String, Object> search(Map<String, String> searchMap);
}
