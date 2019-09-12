package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.SpecificationOption;
import com.pinyougou.pojo.TypeTemplate;
import com.pinyougou.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 类型模板服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-17<p>
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;
    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public void save(TypeTemplate typeTemplate) {
        try{
            typeTemplateMapper.insertSelective(typeTemplate);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        try{
            typeTemplateMapper.updateByPrimaryKeySelective(typeTemplate);
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
            // delete from tb_type_template where id in(?,?,?)
            Example example = new Example(TypeTemplate.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("id", Arrays.asList(ids));
            typeTemplateMapper.deleteByExample(example);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public TypeTemplate findOne(Serializable id) {
        return typeTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<TypeTemplate> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(TypeTemplate typeTemplate, int page, int rows) {
        try{
           // 开启分页
           PageInfo<TypeTemplate> pageInfo = PageHelper.startPage(page, rows)
                   .doSelectPageInfo(new ISelect() {
               @Override
               public void doSelect() {
                   typeTemplateMapper.findAll(typeTemplate);
               }
           });
           return new PageResult(pageInfo.getPages(), pageInfo.getList());
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }

    /** 查询类型模板(id与name) */
    public List<Map<String,Object>> findAllByIdAndName(){
        try{
            return typeTemplateMapper.findAllByIdAndName();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /** 根据主键id查询规格选项数据 */
    public List<Map> findSpecOptionsByTemplateId(Long id){
        try{
            // 获取类型模板一行数据
            TypeTemplate typeTemplate = findOne(id);
            // 获取spec_ids的数据
            // [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            String specIds = typeTemplate.getSpecIds();
            // 把json数组字符串转化List<Map>
            // JSON处理框架：jackson、fastjson
            // JSON.parseObject(); {}
            // JSON.parseArray(); []
            List<Map> specList = JSON.parseArray(specIds, Map.class);

            /**
             * [{"id":27,"text":"网络","options" : [{optionName : ''},{}]},
             * {"id":32,"text":"机身内存","options" : [{optionName : ''},{}]}]
             */
            for (Map map : specList) {
                // 获取id键对应的值
                long specId = Long.valueOf(map.get("id").toString());
                // SELECT * FROM `tb_specification_option` WHERE spec_id = 27
                SpecificationOption so = new SpecificationOption();
                so.setSpecId(specId);
                List<SpecificationOption> options = specificationOptionMapper.select(so);
                map.put("options", options);
            }
            return specList;
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
