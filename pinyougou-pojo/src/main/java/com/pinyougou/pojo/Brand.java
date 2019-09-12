package com.pinyougou.pojo;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 品牌实体类
 * @author LEE.SIU.WAH
 * @email lixiaohua7@163.com
 * @date 2017年12月1日 下午4:59:52
 * @version 1.0
 */
@Data
@Table(name="tb_brand")
public class Brand implements Serializable {

	/**
	 * 主键ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	/**
	 * 品牌名称
	 */
	@Column(name = "name")
	private String name;
	/**
	 * 品牌首字母
	 */
	@Column(name = "first_char")
	private String firstChar;
}
