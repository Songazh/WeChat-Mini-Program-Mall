package com.example.controller;

import com.example.entity.R;
import com.example.entity.Member;
import com.example.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员管理控制器
 */
@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private IMemberService memberService;

    /**
     * 获取会员信息
     */
    @GetMapping("/info")
    public R getMemberInfo(@RequestParam String userId) {
        System.out.println("获取会员信息, userId=" + userId);
        
        // 处理测试账号
        if (userId != null && (userId.startsWith("test_user_openid") || userId.startsWith("mock_openid"))) {
            System.out.println("测试用户，返回模拟会员数据");
            
            // 创建模拟会员对象而不是Map
            Member mockMember = new Member();
            mockMember.setId(1);
            mockMember.setUserId(userId);
            mockMember.setMemberNo("M" + System.currentTimeMillis());
            mockMember.setStatus(1);
            mockMember.setIsPaid(true);  // 设置为已付费会员
            
            // 设置会员有效期为一年后
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, 1);
            mockMember.setExpireTime(calendar.getTime());
            
            mockMember.setPoints(100);
            mockMember.setBalance(new BigDecimal("328.50"));  // 设置余额
            mockMember.setTotalIncome(new BigDecimal("528.50"));  // 设置总收入
            mockMember.setRegisterTime(new Date());
            mockMember.setUpdateTime(new Date());
            
            System.out.println("已设置测试用户为付费会员，过期时间: " + mockMember.getExpireTime());
            
            return R.ok().put("member", mockMember);
        }
        
        // 原有逻辑
        Member member = memberService.getMemberByUserId(userId);
        if (member == null) {
            member = memberService.registerMember(userId);
        }
        return R.ok().put("member", member);
    }
    
    /**
     * 购买会员
     */
    @PostMapping("/purchase")
    public R purchaseMembership(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        Integer period = (Integer) params.get("period");
        
        if (memberId == null || period == null) {
            return R.error("参数错误");
        }
        
        return memberService.purchaseMembership(memberId, period);
    }
    
    /**
     * 支付回调
     */
    @PostMapping("/paymentCallback")
    public R paymentCallback(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        if (orderNo == null) {
            return R.error("参数错误");
        }
        
        memberService.paymentCallback(orderNo);
        return R.ok();
    }
} 