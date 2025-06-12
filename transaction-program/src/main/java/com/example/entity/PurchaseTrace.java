package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 购买追踪实体类
 */
@Data
@TableName("t_purchase_trace")
public class PurchaseTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单ID
     */
    private Integer orderId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 购买者ID(openid)
     */
    private String buyerId;

    /**
     * 分销链接编码
     */
    private String linkCode;

    /**
     * 创建时间
     */
    private Date createTime;
} 