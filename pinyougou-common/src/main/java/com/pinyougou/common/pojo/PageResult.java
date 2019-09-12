package com.pinyougou.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页实体类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-08-15<p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {
    // {pages : 100, rows : [{},{}]}
    // 总页数
    private int pages;
    // 分页数据
    private List<?> rows;
}