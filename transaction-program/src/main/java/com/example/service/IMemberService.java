package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Member;
import com.example.entity.R;

/**
 * 会员服务接口
 */
public interface IMemberService extends IService<Member> {

    /**
     * 注册会员
     */
    Member registerMember(String userId);
    
    /**
     * 根据用户ID获取会员信息
     */
    Member getMemberByUserId(String userId);
    
    /**
     * 检查是否是有效会员
     */
    boolean isValidMember(Integer memberId);
    
    /**
     * 购买会员
     */
    R purchaseMembership(Integer memberId, Integer period);
    
    /**
     * 支付成功回调
     */
    void paymentCallback(String orderNo);
} 