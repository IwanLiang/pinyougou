package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.SellerMapper;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 商家服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-20<p>
 */
@Service
@Transactional
public class SellerServiceImpl implements SellerService{

    @Autowired
    private SellerMapper sellerMapper;

    /** 商家申请入驻 */
    @Override
    public void save(Seller seller) {
        try{
            // 设置审核状态：未审核
            seller.setStatus("0");
            // 设置入驻时间
            seller.setCreateTime(new Date());
            // 保存
            sellerMapper.insertSelective(seller);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean update(Seller seller) {
        try {
            int i = sellerMapper.updateByPrimaryKeySelective(seller);
            return i > 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public Seller findOne(Serializable id) {
        return sellerMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Seller> findAll() {
        return null;
    }

    @Override
    public PageResult findByPage(Seller seller, int page, int rows) {
        try{
           // 开启分页
           PageInfo<Seller> pageInfo = PageHelper.startPage(page, rows)
                   .doSelectPageInfo(new ISelect() {
               @Override
               public void doSelect() {
                    sellerMapper.findAll(seller);
               }
           });
           return new PageResult(pageInfo.getPages(), pageInfo.getList());
        }catch (Exception ex){
           throw new RuntimeException(ex);
        }
    }

    /** 修改商家的审核状态 */
    @Override
    public  void updateStatus(String sellerId, String status){
        try{
            // UPDATE tb_seller SET STATUS = ? WHERE seller_id  = ?
            Seller seller = new Seller();
            seller.setSellerId(sellerId);
            seller.setStatus(status);
            sellerMapper.updateByPrimaryKeySelective(seller);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * 查询回显商家数据
     * @param id
     * @return
     */
    @Override
    public Seller show(String id) {
        try {
            return  sellerMapper.selectByPrimaryKey(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
