package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 分销配置实体类
 */
@Data
@TableName("t_distribution_config")
public class DistributionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 分销等级(1-3)
     */
    private Integer level;

    /**
     * 佣金比例
     */
    private BigDecimal commissionRate;

    /**
     * 积分比例(订单金额的百分比)
     */
    private Integer pointRate;

    /**
     * 状态(0:禁用 1:启用)
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;
} 