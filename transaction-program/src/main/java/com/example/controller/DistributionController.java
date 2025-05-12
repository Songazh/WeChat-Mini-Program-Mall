package com.example.controller;

import com.example.entity.R;
import com.example.service.IDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 分销控制器
 */
@RestController
@RequestMapping("/distribution")
public class DistributionController {
    
    @Autowired
    private IDistributionService distributionService;
    
    /**
     * 申请成为商品分销者
     */
    @PostMapping("/apply")
    public R applyDistributor(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        Integer productId = (Integer) params.get("productId");
        
        if (memberId == null || productId == null) {
            return R.error("参数错误");
        }
        
        return distributionService.applyDistributor(memberId, productId);
    }
    
    /**
     * 生成商品分销二维码
     */
    @GetMapping("/qrcode")
    public void generateQrCode(HttpServletResponse response,
                              @RequestParam(required = false) String code,
                              @RequestParam(required = false) Integer memberId,
                              @RequestParam(required = false) Integer productId) throws IOException {
        // 这里简化实现，实际应该生成二维码图片
        if (code != null) {
            response.getWriter().write("QR Code for: " + code);
        } else if (memberId != null && productId != null) {
            R result = distributionService.generateQrCode(memberId, productId);
            response.getWriter().write("QR Code URL: " + result.get("qrcodeUrl"));
        } else {
            response.getWriter().write("Invalid parameters");
        }
    }
    
    /**
     * 记录分销链接访问
     */
    @PostMapping("/visit")
    public R recordLinkVisit(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String linkCode = (String) params.get("linkCode");
        String visitorId = (String) params.get("visitorId");
        String ip = request.getRemoteAddr();
        String deviceInfo = request.getHeader("User-Agent");
        
        if (linkCode == null) {
            return R.error("参数错误");
        }
        
        distributionService.recordLinkVisit(linkCode, visitorId, ip, deviceInfo);
        return R.ok();
    }
    
    /**
     * 检查是否是商品分销者
     */
    @GetMapping("/check")
    public R checkDistributor(@RequestParam Integer memberId, @RequestParam Integer productId) {
        boolean isDistributor = distributionService.isProductDistributor(memberId, productId);
        return R.ok().put("isDistributor", isDistributor);
    }
    
    /**
     * 获取分销统计信息
     */
    @GetMapping("/statistics")
    public R getStatistics(@RequestParam String memberId) {
        System.out.println("获取分销统计信息, memberId=" + memberId);
        
        // 返回测试数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("productCount", 5);          // 分销商品数量
        statistics.put("visitCount", 120);          // 总访问量
        statistics.put("orderCount", 8);            // 订单数量
        statistics.put("totalIncome", new BigDecimal("328.50"));  // 总收益
        
        return R.ok().put("statistics", statistics);
    }
    
    /**
     * 获取分销商品列表
     */
    @GetMapping("/products")
    public R getDistributionProducts(@RequestParam String memberId, 
                                     @RequestParam(defaultValue = "1") Integer pageNumber,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("获取分销商品列表, memberId=" + memberId);
        
        // 返回测试商品数据
        List<Map<String, Object>> products = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", i);
            product.put("name", "测试商品" + i);
            product.put("price", new BigDecimal("" + (i * 100 + 99) + ".00"));
            product.put("commission", new BigDecimal("" + (i * 10) + ".00"));
            product.put("image", "productImgs/" + (i + 5) + ".png");
            product.put("sales", i * 10);
            product.put("visitCount", i * 25);
            product.put("orderCount", i * 2);
            products.add(product);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", products);
        result.put("total", 5);
        
        return R.ok().put("page", result);
    }
    
    /**
     * 获取分销记录
     */
    @GetMapping("/records")
    public R getDistributionRecords(@RequestParam String memberId,
                                    @RequestParam(defaultValue = "1") Integer pageNumber,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("获取分销记录, memberId=" + memberId);
        
        // 返回测试分销记录
        List<Map<String, Object>> records = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 1; i <= 8; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", i);
            record.put("orderNo", "D" + System.currentTimeMillis() + i);
            record.put("productName", "测试商品" + i);
            record.put("buyerName", "用户" + (i * 10));
            record.put("orderAmount", new BigDecimal("" + (i * 100 + 99) + ".00"));
            record.put("commission", new BigDecimal("" + (i * 10) + ".00"));
            record.put("status", i % 3);  // 0:待结算 1:已结算 2:已取消
            record.put("createTime", sdf.format(new Date()));
            records.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", 8);
        
        return R.ok().put("page", result);
    }
    
    /**
     * 获取收益记录
     */
    @GetMapping("/income")
    public R getIncomeRecords(@RequestParam String memberId,
                              @RequestParam(defaultValue = "1") Integer pageNumber,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("获取收益记录, memberId=" + memberId);
        
        // 返回测试收益记录
        List<Map<String, Object>> incomes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> income = new HashMap<>();
            income.put("id", i);
            income.put("type", i % 3);  // 0:分销收入 1:提现 2:其他
            income.put("amount", new BigDecimal("" + (i * 10) + ".00"));
            income.put("description", "分销商品订单收入");
            income.put("createTime", sdf.format(new Date()));
            incomes.add(income);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", incomes);
        result.put("total", 12);
        result.put("balance", new BigDecimal("328.50"));  // 可用余额
        
        return R.ok().put("page", result);
    }
    
    /**
     * 申请提现
     */
    @PostMapping("/withdraw")
    public R applyWithdraw(@RequestBody Map<String, Object> params) {
        String memberId = (String) params.get("memberId");
        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        Integer accountType = (Integer) params.get("accountType");
        String account = (String) params.get("account");
        String accountName = (String) params.get("accountName");
        
        System.out.println("申请提现: memberId=" + memberId + ", amount=" + amount);
        
        // 返回测试结果
        return R.ok().put("withdrawId", UUID.randomUUID().toString().substring(0, 8));
    }
    
    /**
     * 生成商品分销链接
     */
    @PostMapping("/link")
    public R generateDistributionLink(@RequestBody Map<String, Object> params) {
        String memberId = (String) params.get("memberId");
        Integer productId = (Integer) params.get("productId");
        
        System.out.println("生成分销链接: memberId=" + memberId + ", productId=" + productId);
        
        // 返回测试分销链接
        Map<String, Object> result = new HashMap<>();
        result.put("linkCode", "L" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        result.put("qrcodeUrl", "/image/qrcode_demo.jpg");
        
        return R.ok().put("link", result);
    }
} 