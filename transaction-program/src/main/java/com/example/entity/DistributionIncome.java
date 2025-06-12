package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 分销收益实体类
 */
@Data
@TableName("t_distribution_income")
public class DistributionIncome implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收益ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 获得收益的会员ID
     */
    private Integer memberId;

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
     * 分销级别(1-3)
     */
    private Integer level;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 收益金额
     */
    private BigDecimal incomeAmount;

    /**
     * 积分奖励(第2/3级)
     */
    private Integer pointAmount;

    /**
     * 状态(0:待结算 1:已结算 2:已取消)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 结算时间
     */
    private Date settleTime;
} 