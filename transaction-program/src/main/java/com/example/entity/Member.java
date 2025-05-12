package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员实体类
 */
@TableName("t_member")
@Data
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联微信用户ID(openid)
     */
    private String userId;

    /**
     * 会员编号
     */
    private String memberNo;

    /**
     * 状态(0:禁用 1:正常)
     */
    private Integer status;

    /**
     * 是否付费会员(0:否 1:是)
     */
    private Boolean isPaid;

    /**
     * 会员过期时间
     */
    private Date expireTime;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 可提现余额
     */
    private BigDecimal balance;

    /**
     * 累计收益
     */
    private BigDecimal totalIncome;

    /**
     * 注册时间
     */
    private Date registerTime;

    /**
     * 更新时间
     */
    private Date updateTime;
} 