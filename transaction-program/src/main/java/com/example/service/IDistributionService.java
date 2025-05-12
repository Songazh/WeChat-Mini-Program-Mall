package com.example.service;

import com.example.entity.R;

/**
 * 分销服务接口
 */
public interface IDistributionService {

    /**
     * 申请成为商品分销者
     */
    R applyDistributor(Integer memberId, Integer productId);
    
    /**
     * 创建分销链接
     */
    R createDistributionLink(Integer memberId, Integer productId);
    
    /**
     * 生成商品分销二维码
     */
    R generateQrCode(Integer memberId, Integer productId);
    
    /**
     * 记录分销链接访问
     */
    void recordLinkVisit(String linkCode, String visitorId, String ip, String deviceInfo);
    
    /**
     * 检查是否是商品分销者
     */
    boolean isProductDistributor(Integer memberId, Integer productId);
    
    /**
     * 创建购买追踪
     */
    void createPurchaseTrace(Integer orderId, Integer productId, String buyerId, String linkCode);
    
    /**
     * 处理订单分销
     */
    void processOrderDistribution(Integer orderId);
} 