package com.pinyougou.mapper;

import com.pinyougou.pojo.TypeTemplate;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/**
 * TypeTemplateMapper 数据访问接口
 * @date 2019-08-15 15:23:57
 * @version 1.0
 */
public interface TypeTemplateMapper extends Mapper<TypeTemplate>{

    /** 多条件查询类型模板 */
    List<TypeTemplate> findAll(TypeTemplate typeTemplate);

    /** 查询类型模板(id与name) */
    @Select("select id, name from tb_type_template")
    List<Map<String,Object>> findAllByIdAndName();
}