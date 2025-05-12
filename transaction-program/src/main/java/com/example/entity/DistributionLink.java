package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 分销链接实体类
 */
@TableName("t_distribution_link")
@Data
public class DistributionLink implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 链接ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 链接编码
     */
    private String linkCode;

    /**
     * 分销者ID
     */
    private Integer memberId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expireTime;
    
    /**
     * 二维码URL
     */
    private String qrcodeUrl;
} 