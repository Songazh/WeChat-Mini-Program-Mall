package com.example.service.impl;

import com.example.entity.DistributionLink;
import com.example.entity.ProductDistributor;
import com.example.entity.R;
import com.example.mapper.DistributionLinkMapper;
import com.example.mapper.ProductDistributorMapper;
import com.example.service.IDistributionService;
import com.example.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// 新增导入
import com.example.entity.LinkTrace;
import com.example.entity.PurchaseTrace;
import com.example.entity.DistributionIncome;
import com.example.entity.DistributionConfig;
import com.example.entity.Order;
import com.example.entity.Member;
import com.example.mapper.LinkTraceMapper;
import com.example.mapper.PurchaseTraceMapper;
import com.example.mapper.DistributionIncomeMapper;
import com.example.mapper.DistributionConfigMapper;
import com.example.mapper.OrderMapper;
import com.example.mapper.MemberMapper;

/**
 * 分销服务实现类
 */
@Service
public class DistributionServiceImpl implements IDistributionService {

    @Autowired
    private ProductDistributorMapper distributorMapper;
    
    @Autowired
    private DistributionLinkMapper linkMapper;
    
    @Autowired
    private IMemberService memberService;
    
    // 新增Mapper注入
    @Autowired
    private LinkTraceMapper linkTraceMapper;
    
    @Autowired
    private PurchaseTraceMapper purchaseTraceMapper;
    
    @Autowired
    private DistributionIncomeMapper distributionIncomeMapper;
    
    @Autowired
    private DistributionConfigMapper distributionConfigMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private MemberMapper memberMapper;

    @Override
    @Transactional
    public R applyDistributor(Integer memberId, Integer productId) {
        // 检查是否是有效会员
        if (!memberService.isValidMember(memberId)) {
            return R.error("只有付费会员才能申请成为分销者");
        }
        
        // 检查是否已经是该商品的分销者
        ProductDistributor distributor = distributorMapper.selectByMemberAndProduct(memberId, productId);
        if (distributor != null && distributor.getStatus() == 1) {
            return R.error("您已经是该商品的分销者");
        }
        
        // 创建或更新分销资格
        if (distributor == null) {
            distributor = new ProductDistributor();
            distributor.setMemberId(memberId);
            distributor.setProductId(productId);
            distributor.setStatus(1);
            distributor.setCommissionRate(new BigDecimal("0.10")); // 默认10%佣金
            distributor.setCreateTime(new Date());
            distributorMapper.insert(distributor);
        } else {
            distributor.setStatus(1);
            distributorMapper.updateById(distributor);
        }
        
        return R.ok();
    }

    @Override
    @Transactional
    public R createDistributionLink(Integer memberId, Integer productId) {
        // 检查是否是商品分销者
        if (!isProductDistributor(memberId, productId)) {
            return R.error("您不是该商品的分销者");
        }
        
        // 生成分销码
        String linkCode = generateLinkCode(memberId, productId);
        
        // 创建分销链接记录
        DistributionLink link = new DistributionLink();
        link.setLinkCode(linkCode);
        link.setMemberId(memberId);
        link.setProductId(productId);
        link.setCreateTime(new Date());
        
        // 设置链接有效期(30天)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 30);
        link.setExpireTime(calendar.getTime());
        
        // 为简化实现，这里不实际生成二维码图片
        link.setQrcodeUrl("/distribution/qrcode?code=" + linkCode);
        
        linkMapper.insert(link);
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("linkCode", linkCode);
        resultMap.put("qrcodeUrl", link.getQrcodeUrl());
        
        return R.ok(resultMap);
    }

    @Override
    public R generateQrCode(Integer memberId, Integer productId) {
        // 创建分销链接
        R result = createDistributionLink(memberId, productId);
        if (result.get("code").equals(0)) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            return R.ok().put("qrcodeUrl", data.get("qrcodeUrl"));
        }
        return result;
    }

    @Override
    public void recordLinkVisit(String linkCode, String visitorId, String ip, String deviceInfo) {
        // 验证链接是否有效
        DistributionLink link = linkMapper.selectByLinkCode(linkCode);
        if (link == null || link.getExpireTime().before(new Date())) {
            return; // 链接无效或已过期
        }
        
        // 创建访问记录
        LinkTrace trace = new LinkTrace();
        trace.setLinkId(link.getId());
        trace.setVisitorId(visitorId);
        trace.setIp(ip);
        trace.setDeviceInfo(deviceInfo);
        trace.setVisitTime(new Date());
        
        linkTraceMapper.insert(trace);
    }

    @Override
    public boolean isProductDistributor(Integer memberId, Integer productId) {
        ProductDistributor distributor = distributorMapper.selectByMemberAndProduct(memberId, productId);
        return distributor != null && distributor.getStatus() == 1;
    }

    @Override
    @Transactional
    public void createPurchaseTrace(Integer orderId, Integer productId, String buyerId, String linkCode) {
        // 验证链接是否有效
        DistributionLink link = linkMapper.selectByLinkCode(linkCode);
        if (link == null) {
            return; // 链接无效
        }
        
        // 避免自购分销
        if (buyerId.equals(memberMapper.getOpenidById(link.getMemberId()))) {
            return; // 不能自己购买自己分享的商品获得佣金
        }
        
        // 创建购买追踪记录
        PurchaseTrace trace = new PurchaseTrace();
        trace.setOrderId(orderId);
        trace.setProductId(productId);
        trace.setBuyerId(buyerId);
        trace.setLinkCode(linkCode);
        trace.setCreateTime(new Date());
        
        purchaseTraceMapper.insert(trace);
    }

    @Override
    @Transactional
    public void processOrderDistribution(Integer orderId) {
        // 获取订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null || order.getStatus() != 2) { // 假设状态2表示已完成
            return;
        }
        
        // 获取购买追踪信息
        PurchaseTrace purchaseTrace = purchaseTraceMapper.selectByOrderId(orderId);
        if (purchaseTrace == null || purchaseTrace.getLinkCode() == null) {
            return; // 不是通过分销链接购买的
        }
        
        // 获取分销链接信息
        DistributionLink link = linkMapper.selectByLinkCode(purchaseTrace.getLinkCode());
        if (link == null) {
            return;
        }
        
        // 获取一级分销者
        Member distributor = memberMapper.selectById(link.getMemberId());
        if (distributor == null) {
            return;
        }
        
        // 计算一级分销佣金
        DistributionConfig level1Config = distributionConfigMapper.selectByLevel(1);
        if (level1Config != null && level1Config.getStatus() == 1) {
            BigDecimal commission = order.getTotalPrice().multiply(level1Config.getCommissionRate());
            
            // 创建一级分销收益记录
            DistributionIncome income = new DistributionIncome();
            income.setMemberId(distributor.getId());
            income.setOrderId(orderId);
            income.setProductId(purchaseTrace.getProductId());
            income.setBuyerId(purchaseTrace.getBuyerId());
            income.setLevel(1);
            income.setOrderAmount(order.getTotalPrice());
            income.setCommissionRate(level1Config.getCommissionRate());
            income.setIncomeAmount(commission);
            income.setStatus(0); // 待结算
            income.setCreateTime(new Date());
            
            distributionIncomeMapper.insert(income);
            
            // 更新会员余额
            distributor.setBalance(distributor.getBalance().add(commission));
            distributor.setTotalIncome(distributor.getTotalIncome().add(commission));
            memberMapper.updateById(distributor);
        }
        
        // 处理二级分销 - 查找一级分销者的上级
        ProductDistributor level2Distributor = distributorMapper.selectByProductAndBuyer(
            purchaseTrace.getProductId(), distributor.getOpenId());
        
        if (level2Distributor != null) {
            Member level2Member = memberMapper.selectById(level2Distributor.getMemberId());
            if (level2Member != null) {
                // 计算二级分销积分奖励
                DistributionConfig level2Config = distributionConfigMapper.selectByLevel(2);
                if (level2Config != null && level2Config.getStatus() == 1) {
                    // 计算积分奖励 (订单金额 * 积分比例)
                    int points = order.getTotalPrice().multiply(new BigDecimal(level2Config.getPointRate()))
                                .divide(new BigDecimal(100)).intValue();
                    
                    // 创建二级分销收益记录
                    DistributionIncome income = new DistributionIncome();
                    income.setMemberId(level2Member.getId());
                    income.setOrderId(orderId);
                    income.setProductId(purchaseTrace.getProductId());
                    income.setBuyerId(purchaseTrace.getBuyerId());
                    income.setLevel(2);
                    income.setOrderAmount(order.getTotalPrice());
                    income.setPointAmount(points);
                    income.setStatus(0); // 待结算
                    income.setCreateTime(new Date());
                    
                    distributionIncomeMapper.insert(income);
                    
                    // 更新会员积分
                    level2Member.setPoints(level2Member.getPoints() + points);
                    memberMapper.updateById(level2Member);
                    
                    // 处理三级分销 - 查找二级分销者的上级
                    ProductDistributor level3Distributor = distributorMapper.selectByProductAndBuyer(
                        purchaseTrace.getProductId(), level2Member.getOpenId());
                    
                    if (level3Distributor != null) {
                        Member level3Member = memberMapper.selectById(level3Distributor.getMemberId());
                        if (level3Member != null) {
                            // 计算三级分销积分奖励
                            DistributionConfig level3Config = distributionConfigMapper.selectByLevel(3);
                            if (level3Config != null && level3Config.getStatus() == 1) {
                                // 计算积分奖励 (订单金额 * 积分比例)
                                int level3Points = order.getTotalPrice().multiply(new BigDecimal(level3Config.getPointRate()))
                                            .divide(new BigDecimal(100)).intValue();
                                
                                // 创建三级分销收益记录
                                DistributionIncome level3Income = new DistributionIncome();
                                level3Income.setMemberId(level3Member.getId());
                                level3Income.setOrderId(orderId);
                                level3Income.setProductId(purchaseTrace.getProductId());
                                level3Income.setBuyerId(purchaseTrace.getBuyerId());
                                level3Income.setLevel(3);
                                level3Income.setOrderAmount(order.getTotalPrice());
                                level3Income.setPointAmount(level3Points);
                                level3Income.setStatus(0); // 待结算
                                level3Income.setCreateTime(new Date());
                                
                                distributionIncomeMapper.insert(level3Income);
                                
                                // 更新会员积分
                                level3Member.setPoints(level3Member.getPoints() + level3Points);
                                memberMapper.updateById(level3Member);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 生成唯一分销码
     */
    private String generateLinkCode(Integer memberId, Integer productId) {
        // 基础信息编码
        String baseInfo = memberId + "-" + productId + "-" + System.currentTimeMillis();
        
        // 使用MD5生成唯一码
        String md5Code = DigestUtils.md5DigestAsHex(baseInfo.getBytes());
        
        // 截取前16位作为分销码
        String linkCode = md5Code.substring(0, 16);
        
        // 检查分销码是否已存在
        while (linkMapper.countByLinkCode(linkCode) > 0) {
            // 冲突时重新生成
            baseInfo = memberId + "-" + productId + "-" + System.currentTimeMillis();
            md5Code = DigestUtils.md5DigestAsHex(baseInfo.getBytes());
            linkCode = md5Code.substring(0, 16);
        }
        
        return linkCode;
    }
} 