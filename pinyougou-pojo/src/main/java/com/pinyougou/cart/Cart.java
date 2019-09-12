package com.pinyougou.cart;

import com.pinyougou.pojo.OrderItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车实体类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-09-05<p>
 */
@Data
public class Cart implements Serializable {
    /** 商家ID */
    private String sellerId;
    /** 商家名称 */
    private String sellerName;
    /** 购物车明细集合 */
    private List<OrderItem> orderItems;

}
