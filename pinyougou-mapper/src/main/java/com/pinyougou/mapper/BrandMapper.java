package com.pinyougou.mapper;

import com.pinyougou.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * BrandMapper 数据访问接口
 * @date 2019-08-15 15:23:56
 * @version 1.0
 */
public interface BrandMapper extends Mapper<Brand>{

    /** 多条件查询品牌 */
    List<Brand> findAll(Brand brand);

    /** 查询全部品牌(id与name) */
    @Select("select id, name as text from tb_brand")
    List<Map<String,Object>> findAllByIdAndName();
}