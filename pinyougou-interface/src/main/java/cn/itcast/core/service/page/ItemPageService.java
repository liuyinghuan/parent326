package cn.itcast.core.service.page;

public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId SPU ID
     */
    public boolean genHtml(Long goodsId);

}
