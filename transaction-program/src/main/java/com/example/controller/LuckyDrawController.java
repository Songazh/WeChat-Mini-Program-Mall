package com.example.controller;

import com.example.entity.R;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 抽奖中心控制器
 */
@RestController
@RequestMapping("/lucky")
public class LuckyDrawController {
    
    /**
     * 获取抽奖活动列表
     */
    @GetMapping("/activities")
    public R getLuckyDrawActivities(@RequestParam(required = false) Integer memberId) {
        System.out.println("获取抽奖活动列表, memberId=" + memberId);
        
        // 返回模拟数据
        List<Map<String, Object>> activities = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 生成当前日期后3天的日期
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        Date after3days = calendar.getTime();
        
        // 活动1：每日免费抽奖
        Map<String, Object> activity1 = new HashMap<>();
        activity1.put("id", 1);
        activity1.put("title", "每日免费抽奖");
        activity1.put("description", "每天可免费抽奖一次，有机会获得积分奖励");
        activity1.put("imageUrl", "activity/free_draw.jpg");
        activity1.put("startTime", sdf.format(now));
        activity1.put("endTime", sdf.format(after3days));
        activity1.put("status", 1); // 1-进行中 2-已结束 3-未开始
        activity1.put("prizePool", "积分、优惠券、实物奖品");
        activity1.put("timesLimit", 1); // 每天可参与次数
        activity1.put("timesUsed", 0); // 今日已参与次数
        activity1.put("pointsRequired", 0); // 参与需要的积分
        activities.add(activity1);
        
        // 活动2：会员专属抽奖
        Map<String, Object> activity2 = new HashMap<>();
        activity2.put("id", 2);
        activity2.put("title", "会员专属抽奖");
        activity2.put("description", "会员专属抽奖活动，丰厚奖品等你来拿");
        activity2.put("imageUrl", "activity/member_draw.jpg");
        activity2.put("startTime", sdf.format(now));
        activity2.put("endTime", sdf.format(after3days));
        activity2.put("status", 1);
        activity2.put("prizePool", "优惠券、实物奖品、高额积分");
        activity2.put("timesLimit", 5);
        activity2.put("timesUsed", 0);
        activity2.put("pointsRequired", 10); // 每次需要10积分
        activities.add(activity2);
        
        // 活动3：积分抽奖
        Map<String, Object> activity3 = new HashMap<>();
        activity3.put("id", 3);
        activity3.put("title", "积分抽奖");
        activity3.put("description", "使用积分参与抽奖，赢取超值奖品");
        activity3.put("imageUrl", "activity/points_draw.jpg");
        activity3.put("startTime", sdf.format(now));
        activity3.put("endTime", sdf.format(after3days));
        activity3.put("status", 1);
        activity3.put("prizePool", "现金红包、优惠券、实物奖品");
        activity3.put("timesLimit", 10);
        activity3.put("timesUsed", 0);
        activity3.put("pointsRequired", 30); // 每次需要30积分
        activities.add(activity3);
        
        return R.ok().put("activities", activities);
    }
    
    /**
     * 获取抽奖奖品列表
     */
    @GetMapping("/prizes")
    public R getLuckyDrawPrizes(@RequestParam Integer activityId) {
        System.out.println("获取抽奖奖品列表, activityId=" + activityId);
        
        // 返回模拟数据
        List<Map<String, Object>> prizes = new ArrayList<>();
        
        // 根据活动ID生成不同的奖品
        if (activityId == 1) { // 每日免费抽奖
            // 奖品1：10积分
            Map<String, Object> prize1 = new HashMap<>();
            prize1.put("id", 1);
            prize1.put("name", "10积分");
            prize1.put("type", 1); // 1-积分 2-优惠券 3-实物 4-红包
            prize1.put("value", 10);
            prize1.put("imageUrl", "prizes/points_10.png");
            prize1.put("probability", 40); // 概率：40%
            prizes.add(prize1);
            
            // 奖品2：50积分
            Map<String, Object> prize2 = new HashMap<>();
            prize2.put("id", 2);
            prize2.put("name", "50积分");
            prize2.put("type", 1);
            prize2.put("value", 50);
            prize2.put("imageUrl", "prizes/points_50.png");
            prize2.put("probability", 30); // 概率：30%
            prizes.add(prize2);
            
            // 奖品3：100积分
            Map<String, Object> prize3 = new HashMap<>();
            prize3.put("id", 3);
            prize3.put("name", "100积分");
            prize3.put("type", 1);
            prize3.put("value", 100);
            prize3.put("imageUrl", "prizes/points_100.png");
            prize3.put("probability", 20); // 概率：20%
            prizes.add(prize3);
            
            // 奖品4：满10减5优惠券
            Map<String, Object> prize4 = new HashMap<>();
            prize4.put("id", 4);
            prize4.put("name", "满10减5券");
            prize4.put("type", 2);
            prize4.put("value", 5);
            prize4.put("imageUrl", "prizes/coupon_5.png");
            prize4.put("probability", 10); // 概率：10%
            prizes.add(prize4);
        } else if (activityId == 2) { // 会员专属抽奖
            // 奖品1：50积分
            Map<String, Object> prize1 = new HashMap<>();
            prize1.put("id", 5);
            prize1.put("name", "50积分");
            prize1.put("type", 1);
            prize1.put("value", 50);
            prize1.put("imageUrl", "prizes/points_50.png");
            prize1.put("probability", 30); // 概率：30%
            prizes.add(prize1);
            
            // 奖品2：100积分
            Map<String, Object> prize2 = new HashMap<>();
            prize2.put("id", 6);
            prize2.put("name", "100积分");
            prize2.put("type", 1);
            prize2.put("value", 100);
            prize2.put("imageUrl", "prizes/points_100.png");
            prize2.put("probability", 25); // 概率：25%
            prizes.add(prize2);
            
            // 奖品3：满50减20优惠券
            Map<String, Object> prize3 = new HashMap<>();
            prize3.put("id", 7);
            prize3.put("name", "满50减20券");
            prize3.put("type", 2);
            prize3.put("value", 20);
            prize3.put("imageUrl", "prizes/coupon_20.png");
            prize3.put("probability", 20); // 概率：20%
            prizes.add(prize3);
            
            // 奖品4：满100减50优惠券
            Map<String, Object> prize4 = new HashMap<>();
            prize4.put("id", 8);
            prize4.put("name", "满100减50券");
            prize4.put("type", 2);
            prize4.put("value", 50);
            prize4.put("imageUrl", "prizes/coupon_50.png");
            prize4.put("probability", 15); // 概率：15%
            prizes.add(prize4);
            
            // 奖品5：5元红包
            Map<String, Object> prize5 = new HashMap<>();
            prize5.put("id", 9);
            prize5.put("name", "5元红包");
            prize5.put("type", 4);
            prize5.put("value", 5);
            prize5.put("imageUrl", "prizes/redpacket_5.png");
            prize5.put("probability", 10); // 概率：10%
            prizes.add(prize5);
        } else { // 积分抽奖或其他活动
            // 奖品1：100积分
            Map<String, Object> prize1 = new HashMap<>();
            prize1.put("id", 10);
            prize1.put("name", "100积分");
            prize1.put("type", 1);
            prize1.put("value", 100);
            prize1.put("imageUrl", "prizes/points_100.png");
            prize1.put("probability", 20); // 概率：20%
            prizes.add(prize1);
            
            // 奖品2：200积分
            Map<String, Object> prize2 = new HashMap<>();
            prize2.put("id", 11);
            prize2.put("name", "200积分");
            prize2.put("type", 1);
            prize2.put("value", 200);
            prize2.put("imageUrl", "prizes/points_200.png");
            prize2.put("probability", 15); // 概率：15%
            prizes.add(prize2);
            
            // 奖品3：满100减50优惠券
            Map<String, Object> prize3 = new HashMap<>();
            prize3.put("id", 12);
            prize3.put("name", "满100减50券");
            prize3.put("type", 2);
            prize3.put("value", 50);
            prize3.put("imageUrl", "prizes/coupon_50.png");
            prize3.put("probability", 25); // 概率：25%
            prizes.add(prize3);
            
            // 奖品4：10元红包
            Map<String, Object> prize4 = new HashMap<>();
            prize4.put("id", 13);
            prize4.put("name", "10元红包");
            prize4.put("type", 4);
            prize4.put("value", 10);
            prize4.put("imageUrl", "prizes/redpacket_10.png");
            prize4.put("probability", 10); // 概率：10%
            prizes.add(prize4);
            
            // 奖品5：实物礼品
            Map<String, Object> prize5 = new HashMap<>();
            prize5.put("id", 14);
            prize5.put("name", "精美礼品");
            prize5.put("type", 3);
            prize5.put("value", 0);
            prize5.put("imageUrl", "prizes/gift.png");
            prize5.put("probability", 5); // 概率：5%
            prizes.add(prize5);
            
            // 奖品6：谢谢参与
            Map<String, Object> prize6 = new HashMap<>();
            prize6.put("id", 15);
            prize6.put("name", "谢谢参与");
            prize6.put("type", 0);
            prize6.put("value", 0);
            prize6.put("imageUrl", "prizes/thanks.png");
            prize6.put("probability", 25); // 概率：25%
            prizes.add(prize6);
        }
        
        return R.ok().put("prizes", prizes);
    }
    
    /**
     * 执行抽奖
     */
    @PostMapping("/draw")
    public R drawLuckyPrize(@RequestBody Map<String, Object> params) {
        Integer memberId = (Integer) params.get("memberId");
        Integer activityId = (Integer) params.get("activityId");
        
        System.out.println("执行抽奖, memberId=" + memberId + ", activityId=" + activityId);
        
        if (memberId == null || activityId == null) {
            return R.error("参数错误");
        }
        
        // 模拟抽奖结果
        Random random = new Random();
        
        // 根据活动ID决定抽奖结果
        Map<String, Object> result = new HashMap<>();
        
        // 简单模拟，实际应该根据概率分布
        int prizeIndex = random.nextInt(4); // 0-3，对应4种奖品
        
        if (activityId == 1) { // 每日免费抽奖
            switch (prizeIndex) {
                case 0:
                    result.put("prizeId", 1);
                    result.put("prizeName", "10积分");
                    result.put("prizeType", 1);
                    result.put("prizeValue", 10);
                    result.put("prizeImageUrl", "prizes/points_10.png");
                    break;
                case 1:
                    result.put("prizeId", 2);
                    result.put("prizeName", "50积分");
                    result.put("prizeType", 1);
                    result.put("prizeValue", 50);
                    result.put("prizeImageUrl", "prizes/points_50.png");
                    break;
                case 2:
                    result.put("prizeId", 3);
                    result.put("prizeName", "100积分");
                    result.put("prizeType", 1);
                    result.put("prizeValue", 100);
                    result.put("prizeImageUrl", "prizes/points_100.png");
                    break;
                case 3:
                    result.put("prizeId", 4);
                    result.put("prizeName", "满10减5券");
                    result.put("prizeType", 2);
                    result.put("prizeValue", 5);
                    result.put("prizeImageUrl", "prizes/coupon_5.png");
                    break;
            }
        } else if (activityId == 2) { // 会员专属抽奖
            switch (prizeIndex) {
                case 0:
                    result.put("prizeId", 5);
                    result.put("prizeName", "50积分");
                    result.put("prizeType", 1);
                    result.put("prizeValue", 50);
                    result.put("prizeImageUrl", "prizes/points_50.png");
                    break;
                case 1:
                    result.put("prizeId", 6);
                    result.put("prizeName", "100积分");
                    result.put("prizeType", 1);
                    result.put("prizeValue", 100);
                    result.put("prizeImageUrl", "prizes/points_100.png");
                    break;
                case 2:
                    result.put("prizeId", 7);
                    result.put("prizeName", "满50减20券");
                    result.put("prizeType", 2);
                    result.put("prizeValue", 20);
                    result.put("prizeImageUrl", "prizes/coupon_20.png");
                    break;
                case 3:
                    result.put("prizeId", 9);
                    result.put("prizeName", "5元红包");
                    result.put("prizeType", 4);
                    result.put("prizeValue", 5);
                    result.put("prizeImageUrl", "prizes/redpacket_5.png");
                    break;
            }
        } else { // 积分抽奖或其他活动
            switch (prizeIndex) {
                case 0:
                    result.put("prizeId", 10);
                    result.put("prizeName", "100积分");
                    result.put("prizeType", 1);
                    result.put("prizeValue", 100);
                    result.put("prizeImageUrl", "prizes/points_100.png");
                    break;
                case 1:
                    result.put("prizeId", 12);
                    result.put("prizeName", "满100减50券");
                    result.put("prizeType", 2);
                    result.put("prizeValue", 50);
                    result.put("prizeImageUrl", "prizes/coupon_50.png");
                    break;
                case 2:
                    result.put("prizeId", 13);
                    result.put("prizeName", "10元红包");
                    result.put("prizeType", 4);
                    result.put("prizeValue", 10);
                    result.put("prizeImageUrl", "prizes/redpacket_10.png");
                    break;
                case 3:
                    result.put("prizeId", 15);
                    result.put("prizeName", "谢谢参与");
                    result.put("prizeType", 0);
                    result.put("prizeValue", 0);
                    result.put("prizeImageUrl", "prizes/thanks.png");
                    break;
            }
        }
        
        // 生成抽奖记录ID
        result.put("recordId", UUID.randomUUID().toString().substring(0, 10));
        result.put("drawTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        
        return R.ok().put("result", result);
    }
    
    /**
     * 获取抽奖记录
     */
    @GetMapping("/records")
    public R getDrawRecords(@RequestParam Integer memberId, 
                           @RequestParam(defaultValue = "1") Integer pageNumber, 
                           @RequestParam(defaultValue = "10") Integer pageSize) {
        System.out.println("获取抽奖记录, memberId=" + memberId);
        
        // 返回模拟数据
        List<Map<String, Object>> records = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 生成10条模拟记录
        for (int i = 0; i < 10; i++) {
            Map<String, Object> record = new HashMap<>();
            int activityId = (i % 3) + 1; // 1-3
            
            record.put("id", UUID.randomUUID().toString().substring(0, 10));
            record.put("activityId", activityId);
            
            // 根据活动ID设置活动名称
            if (activityId == 1) {
                record.put("activityName", "每日免费抽奖");
            } else if (activityId == 2) {
                record.put("activityName", "会员专属抽奖");
            } else {
                record.put("activityName", "积分抽奖");
            }
            
            // 设置随机奖品
            int prizeType = (i % 3) + 1; // 1-3
            int prizeValue = 0;
            String prizeName = "";
            
            if (prizeType == 1) { // 积分
                prizeValue = (i + 1) * 10;
                prizeName = prizeValue + "积分";
            } else if (prizeType == 2) { // 优惠券
                prizeValue = (i + 1) * 5;
                prizeName = "满" + (prizeValue * 2) + "减" + prizeValue + "券";
            } else if (prizeType == 3) { // 实物
                prizeValue = 0;
                prizeName = "精美礼品";
            }
            
            record.put("prizeType", prizeType);
            record.put("prizeName", prizeName);
            record.put("prizeValue", prizeValue);
            
            // 设置抽奖时间，每条记录比前一条早1小时
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, -i);
            record.put("drawTime", sdf.format(calendar.getTime()));
            
            records.add(record);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", 10);
        
        return R.ok().put("page", result);
    }
    
    /**
     * 领取奖品
     */
    @PostMapping("/claim")
    public R claimPrize(@RequestBody Map<String, Object> params) {
        String recordId = (String) params.get("recordId");
        Integer memberId = (Integer) params.get("memberId");
        String address = (String) params.get("address");
        String phone = (String) params.get("phone");
        
        System.out.println("领取奖品, recordId=" + recordId + ", memberId=" + memberId);
        
        if (recordId == null || memberId == null) {
            return R.error("参数错误");
        }
        
        // 模拟领取成功
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "奖品领取成功");
        
        return R.ok(result);
    }
} 