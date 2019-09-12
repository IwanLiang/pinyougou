package com.pinyougou.service;

import com.pinyougou.es.EsItem;

import java.util.List;
import java.util.Map; /**
 * ItemSearchService接口
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-27<p>
 */
public interface ItemSearchService {

    /**
     * 商品搜索方法
     * @param params 搜索条件
     * @return 搜索结果
     */
    Map<String,Object> search(Map<String, Object> params);

    /** 添加或修改商品索引 */
    void saveOrUpdate(List<EsItem> items);

    /** 删除商品索引 */
    void delete(List<Long> goodsIds);
}
