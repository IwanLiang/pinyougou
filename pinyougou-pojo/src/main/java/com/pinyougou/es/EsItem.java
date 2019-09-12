package com.pinyougou.es;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * SKU商品实体类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2018-04-15<p>
 */
/**
 * 把实体类映射成文档
 * indexName : 索引库名称
 * type: 类型(相当于数据库表)
 */
@Document(indexName = "pinyougou", type = "item")
public class EsItem implements Serializable {
    // SKU商品id
    @Id // 主键
    @Field(store = true, index = true, type = FieldType.Long)
    private Long id;
    // SKU商品标题
    @Field(store = true,
            index = true,
            type = FieldType.Text,
            analyzer = "ik_smart",
            searchAnalyzer = "ik_smart",
            copyTo = "keywords")
    private String title;
    // SKU商品价格
    @Field(store = true, index = true, type = FieldType.Double)
    private Double price;
    // SKU商品图片
    @Field(store = true, index = false, type = FieldType.Text)
    private String image;
    // SKU商品关联的SPU的id
    @Field(store = true, index = true, type = FieldType.Long)
    private Long goodsId;

    // SKU商品关联的商品分类
    // FieldType.Keyword : 不分词
    @Field(store = true, index = true, type = FieldType.Keyword, copyTo = "keywords")
    private String category;
    // SKU商品关联的品牌
    @Field(store = true, index = true, type = FieldType.Keyword, copyTo = "keywords")
    private String brand;
    // SKU商品关联的商家
    @Field(store = true, index = true, type = FieldType.Keyword, copyTo = "keywords")
    private String seller;

	// SKU商品更新时间
    @Field(store = true, index = true,
            type = FieldType.Date,
            pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 规格选项: 嵌套Field */
    @Field(store = true, index = true, type = FieldType.Nested)
    private Map<String,String> spec;

    /** setter and getter method */
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }
    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public Long getGoodsId() {
        return goodsId;
    }
    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getSeller() {
        return seller;
    }
    public void setSeller(String seller) {
        this.seller = seller;
    }
	public Date getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, String> getSpec() {
        return spec;
    }

    public void setSpec(Map<String, String> spec) {
        this.spec = spec;
    }
}