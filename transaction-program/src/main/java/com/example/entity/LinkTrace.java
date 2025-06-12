package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 链接追踪实体类
 */
@Data
@TableName("t_link_trace")
public class LinkTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 链接ID
     */
    private Integer linkId;

    /**
     * 访问者ID(openid)
     */
    private String visitorId;

    /**
     * 访问IP
     */
    private String ip;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 访问时间
     */
    private Date visitTime;
} 