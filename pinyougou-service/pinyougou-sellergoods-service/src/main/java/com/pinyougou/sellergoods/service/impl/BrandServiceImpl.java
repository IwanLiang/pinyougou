package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.Brand;
import com.pinyougou.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.*;

/**
 * 品牌服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-14<p>
 */
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public void save(Brand brand) {
        try {
            // 选择性添加
            brandMapper.insertSelective(brand);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Brand brand) {
        try {
            // 选择性修改
            brandMapper.updateByPrimaryKeySelective(brand);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {
        try{
            // delete from tb_brand where id in(?,?,?)
            Example example = new Example(Brand.class);
            // 删除条件
            Example.Criteria criteria = example.createCriteria();
            // id in(?,?,?)
            criteria.andIn("id", Arrays.asList(ids));
            brandMapper.deleteByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Brand findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Brand> findAll() {
       return brandMapper.selectAll();
    }

    @Override
    public PageResult findByPage(Brand brand, int page, int rows) {
        try{
           // 开启分页
           PageInfo<Brand> pageInfo = PageHelper.startPage(page, rows)
                   .doSelectPageInfo(new ISelect() {
               @Override
               public void doSelect() {
                    brandMapper.findAll(brand);
               }
           });

           return new PageResult(pageInfo.getPages(), pageInfo.getList());
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }

    /** 查询全部品牌(id与name) */
    public List<Map<String,Object>> findAllByIdAndName(){
        try{
            // [{id:1, text : '华为'},{id:1, text : '小米'},{id:1, text : '红米'}]
            return brandMapper.findAllByIdAndName();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
