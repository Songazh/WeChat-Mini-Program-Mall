package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品分销者实体类
 */
@TableName("t_product_distributor")
@Data
public class ProductDistributor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 会员ID
     */
    private Integer memberId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 状态(0:无效 1:有效)
     */
    private Integer status;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 创建时间
     */
    private Date createTime;
} 