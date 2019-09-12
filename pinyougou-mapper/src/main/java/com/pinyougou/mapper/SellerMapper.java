package com.pinyougou.mapper;

import com.pinyougou.pojo.Seller;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * SellerMapper 数据访问接口
 * @date 2019-08-15 15:23:56
 * @version 1.0
 */
public interface SellerMapper extends Mapper<Seller>{

    /** 多条件查询商家 */
    List<Seller> findAll(Seller seller);
}