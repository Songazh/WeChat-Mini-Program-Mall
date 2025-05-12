package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单主表
 */
@TableName("t_order")
@Data
public class Order {

    private Integer id; // 编号

    private String orderNo; // 订单号

    private String userId; // openId微信用户ID

    @TableField(select = false)
    private WxUserInfo wxUserInfo; // 微信用户

    private BigDecimal totalPrice; // 总价

    private String address; // 收货地址

    private String consignee; // 收货人

    private String telNumber; // 联系电话

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date createDate; // 订单创建日期

    @JsonSerialize(using=CustomDateTimeSerializer.class)
    private Date payDate; // 订单支付日期

    private Integer status=1; // 订单状态 1 未支付 2 已经支付/待收货 3 退款/退货

    @TableField(select = false, exist = false)
    private OrderDetail[] goods; // 订单商品详情

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public WxUserInfo getWxUserInfo() {
        return wxUserInfo;
    }

    public void setWxUserInfo(WxUserInfo wxUserInfo) {
        this.wxUserInfo = wxUserInfo;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public OrderDetail[] getGoods() {
        return goods;
    }

    public void setGoods(OrderDetail[] goods) {
        this.goods = goods;
    }

}
