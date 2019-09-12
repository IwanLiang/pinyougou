package com.pinyougou.mapper;

import com.pinyougou.pojo.Specification;
import tk.mybatis.mapper.common.Mapper;

import com.pinyougou.pojo.SpecificationOption;

/**
 * SpecificationOptionMapper 数据访问接口
 * @date 2019-08-15 15:23:57
 * @version 1.0
 */
public interface SpecificationOptionMapper extends Mapper<SpecificationOption>{

    /** 批量插入数据 */
    void save(Specification specification);
}