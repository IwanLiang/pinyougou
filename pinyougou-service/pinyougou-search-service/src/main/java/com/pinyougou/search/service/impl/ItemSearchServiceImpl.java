package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.es.EsItem;
import com.pinyougou.search.dao.EsItemDao;
import com.pinyougou.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品搜索服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-27<p>
 */
@Service
public class ItemSearchServiceImpl implements ItemSearchService{

    @Autowired
    private ElasticsearchTemplate esTemplate;
    @Autowired
    private EsItemDao esItemDao;

    /** 添加或修改商品索引 */
    public void saveOrUpdate(List<EsItem> items){
        try{
            esItemDao.saveAll(items);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 删除商品索引 */
    public void delete(List<Long> goodsIds){
        try{
            // 创建删除查询对象
            DeleteQuery deleteQuery = new DeleteQuery();
            deleteQuery.setIndex("pinyougou");
            deleteQuery.setType("item");
            deleteQuery.setQuery(QueryBuilders.termsQuery("goodsId", goodsIds));
            esTemplate.delete(deleteQuery);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 商品搜索 */
    @Override
    public Map<String, Object> search(Map<String, Object> params) {
        try{

            // 创建原生的搜索查询构建对象
            NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
            // 设置默认查询全部(添加搜索条件)
            builder.withQuery(QueryBuilders.matchAllQuery());

            // 获取搜索关键字
            String keywords = (String)params.get("keywords");
            // 判断关键字是否为空
            if (StringUtils.isNoneBlank(keywords)){

                /** ############# 1. 按关键字搜索 ############# */
                // 设置根据多个条件进行搜索
                builder.withQuery(QueryBuilders.multiMatchQuery(keywords,
                        "title","brand","category","seller"));


                /** ############# 2. 搜索高亮 ############# */
                // 创建高亮字段
                HighlightBuilder.Field field = new HighlightBuilder.Field("title");
                // 设置高亮格式器前缀
                field.preTags("<font color='red'>");
                // 设置高亮格式器后缀
                field.postTags("</font>");
                // 设置高亮内容文本截断
                field.fragmentSize(50);

                // 设置高亮字段Field
                builder.withHighlightFields(field);
            }


            // { "keywords": "", "category": "手机", "brand": "三星",
            // "price": "1000-1500", "spec": { "网络": "联通4G", "机身内存": "128G" } }
            /** ############# 3. 搜索过滤 ############# */
            // 3.1 创建布尔查询构建对象，用来组装过滤条件
            BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
            // 3.2 组装过滤条件
            // 3.2.1 按商品分类过滤
            String category = (String) params.get("category");
            if (StringUtils.isNoneBlank(category)){
                // should: OR  must: AND mustNot: NOT
                boolBuilder.must(QueryBuilders.termQuery("category", category));

            }
            // 3.2.2 按商品品牌过滤
            String brand = (String) params.get("brand");
            if (StringUtils.isNoneBlank(brand)){
                // should: OR  must: AND mustNot: NOT
                boolBuilder.must(QueryBuilders.termQuery("brand", brand));
            }
            // 3.2.3 按商品规格选项过滤(嵌套Field)
            // { "网络": "联通4G", "机身内存": "128G" }
            Map<String,String> specMap = (Map<String,String>) params.get("spec");
            if (specMap != null && specMap.size() > 0){
                // 迭代Map集合
                for (String key : specMap.keySet()) {
                    // 嵌套Field的名称
                    String field = "spec." + key + ".keyword";
                    // String path: 最外面的Field的名称
                    // QueryBuilder query: 查询条件
                    // ScoreMode scoreMode: 分数模式
                    boolBuilder.must(QueryBuilders.nestedQuery("spec",
                            QueryBuilders.termQuery(field, specMap.get(key)),
                            ScoreMode.None));
                }
            }

            // 3.2.4 按商品价格区间过滤
            String price = (String) params.get("price");
            if (StringUtils.isNoneBlank(price)){
                // 0-500,1000-1500,3000-*
                String[] priceArr = price.split("-");
                // 创建范围查询构建对象
                RangeQueryBuilder rangeBuilder = new RangeQueryBuilder("price");
                if ("*".equals(priceArr[1])){
                    // 价格大于等于3000
                    rangeBuilder.gte(priceArr[0]);
                }else {
                    // 价格从哪里开始  到  哪里结束
                    rangeBuilder.from(priceArr[0], true).to(priceArr[1],false);
                }
                // should: OR  must: AND mustNot: NOT
                boolBuilder.must(rangeBuilder);
            }

            // 3.3 设置过滤查询
            builder.withFilter(boolBuilder);


            // 创建搜索查询对象
            SearchQuery query  = builder.build();

            /** ############# 4. 搜索分页 ############# */
            // 获取当前页码
            Integer curPage = (Integer) params.get("page");
            if (curPage == null){
                curPage = 1;
            }
            // 搜索查询对象设置分页对象(注意：分页的第一个参数：当前页码 - 1)
            query.setPageable(PageRequest.of(curPage - 1, 10));


            /** ############# 5. 搜索排序 ############# */
            // 获取排序参数
            String sortField = (String)params.get("sortField");
            String sortValue = (String)params.get("sortValue");
            if (StringUtils.isNoneBlank(sortField) && StringUtils.isNoneBlank(sortValue)){
                // 创建排序对象
                Sort sort = new Sort("ASC".equals(sortValue)
                        ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
                // 搜索查询对象添加排序对象
                query.addSort(sort);
            }

            // 搜索分页，得到合计分页对象
            AggregatedPage<EsItem> page = esTemplate.queryForPage(query, EsItem.class,
                    new SearchResultMapper() { // 对搜索结果进行转化(获取高亮内容)
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse sr,
                                                        Class<T> aClass, Pageable pageable) {
                    // 定义List集合封装搜索结果
                    List<T> content = new ArrayList<>();
                    // 获取搜索命中到得文档对象
                    SearchHits hits = sr.getHits();
                    // 迭代搜索命中的文档集合
                    for (SearchHit hit : hits) {
                        // SearchHit：代表命中的一篇文档
                        // 获取一篇文档对应的json字符串，转化成EsItem对象
                        EsItem esItem = JSON.parseObject(hit.getSourceAsString(),EsItem.class);
                        System.out.println(hit.getSourceAsString());

                        // 获取标题高亮字段
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        if (highlightField != null){
                            // 获取标题高亮内容
                            String title = highlightField.getFragments()[0].toString();
                            System.out.println("title = " + title);
                            // 设置标题高亮内容
                            esItem.setTitle(title);
                        }
                        content.add((T)esItem);
                    }
                    return new AggregatedPageImpl(content, pageable, sr.getHits().getTotalHits());
                }
            });

            Map<String, Object> data = new HashMap<>();
            // 获取总记录数
            data.put("total", page.getTotalElements());
            // 获取分页数据
            data.put("rows", page.getContent());
            // 获取总页数
            data.put("totalPages", page.getTotalPages());
            return data;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
