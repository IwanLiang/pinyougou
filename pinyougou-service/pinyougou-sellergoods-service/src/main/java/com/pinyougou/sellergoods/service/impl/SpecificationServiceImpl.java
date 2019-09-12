package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationMapper;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.pojo.Specification;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 规格服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-17<p>
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(Specification specification) {
        try{
            // 1. 往tb_specification规格表插入一行数据
            specificationMapper.insertSelective(specification);
            System.out.println("主键ID:" + specification.getId());

            // 2. 往tb_specification规格选项表插入多行数据
            specificationOptionMapper.save(specification);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(Specification specification) {
        try{
            // 1. 修改tb_specification规格表中的数据
            specificationMapper.updateByPrimaryKeySelective(specification);

            // 2. 修改tb_specification_option规格表
            // 2.1 删除tb_specification_option表中的数据
            // DELETE FROM tb_specification_option WHERE spec_id = 32
            SpecificationOption so  = new SpecificationOption();
            so.setSpecId(specification.getId());
            specificationOptionMapper.delete(so);

            // 2.2 往tb_specification_option表插入数据
            specificationOptionMapper.save(specification);

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
            // DELETE FROM tb_specification_option WHERE spec_id IN(?,?,?)
            Example example = new Example(SpecificationOption.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("specId", Arrays.asList(ids));
            specificationOptionMapper.deleteByExample(example);

            // DELETE FROM tb_specification WHERE id IN(?,?,?)
            example = new Example(Specification.class);
            criteria = example.createCriteria();
            criteria.andIn("id", Arrays.asList(ids));
            specificationMapper.deleteByExample(example);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Specification findOne(Serializable id) {
        return null;
    }

    @Override
    public List<Specification> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Specification specification, int page, int rows) {
        try{
           // 开启分页
           PageInfo<Specification> pageInfo = PageHelper.startPage(page, rows)
                   .doSelectPageInfo(new ISelect() {
               @Override
               public void doSelect() {
                    specificationMapper.findAll(specification);
               }
           });
           return new PageResult(pageInfo.getPages(), pageInfo.getList());
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }

    /** 根据规格id查询规格选项 */
    public List<SpecificationOption> findSpecOptions(Long id){
        try{
            // SELECT * FROM tb_specification_option WHERE spec_id = 32
            SpecificationOption so = new SpecificationOption();
            so.setSpecId(id);
            return specificationOptionMapper.select(so);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 查询全部规格 */
    public List<Map<String,Object>> findAllByIdAndName(){
        try{
            return specificationMapper.findAllByIdAndName();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
