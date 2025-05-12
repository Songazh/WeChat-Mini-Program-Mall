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
        // TODO: 实现访问记录逻辑
    }

    @Override
    public boolean isProductDistributor(Integer memberId, Integer productId) {
        ProductDistributor distributor = distributorMapper.selectByMemberAndProduct(memberId, productId);
        return distributor != null && distributor.getStatus() == 1;
    }

    @Override
    public void createPurchaseTrace(Integer orderId, Integer productId, String buyerId, String linkCode) {
        // TODO: 实现购买追踪逻辑
    }

    @Override
    public void processOrderDistribution(Integer orderId) {
        // TODO: 实现订单分销处理逻辑
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