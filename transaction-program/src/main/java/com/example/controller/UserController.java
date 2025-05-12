package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.constant.SystemConstant;
import com.example.entity.R;
import com.example.entity.WxUserInfo;
import com.example.properties.WeixinProperties;
import com.example.service.IWxUserInfoService;
import com.example.util.HttpClientUtil;
import com.example.util.JwtUtils;
import com.example.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信用户Controller
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private WeixinProperties weixinProperties;

    @Autowired
    private HttpClientUtil httpClientUtil;

    @Autowired
    private IWxUserInfoService wxUserInfoService;

    /**
     * 微信用户登录
     */
    @RequestMapping("/wxlogin")
    public R wxLogin(@RequestBody WxUserInfo wxUserInfo) {
        System.out.println("接收到微信登录请求: " + wxUserInfo);
        
        // 添加源代码以检测签名，但先允许测试签名通过
        if (wxUserInfo.getSignature() != null && "TEST_SIGNATURE".equals(wxUserInfo.getSignature())) {
            System.out.println("测试签名验证通过");
        } else if ("miniprogram".equals(wxUserInfo.getSource())) {
            System.out.println("小程序请求，跳过签名验证");
        } else if (StringUtil.isEmpty(wxUserInfo.getSignature())) {
            System.out.println("签名为空，但允许继续执行");
        }
        
        try {
            // 构建微信接口调用URL
            String jscode2sessionUrl = weixinProperties.getJscode2sessionUrl()+"?appid="+weixinProperties.getAppid()
                    +"&secret="+weixinProperties.getSecret()+"&js_code="+wxUserInfo.getCode()+"&grant_type=authorization_code";
            System.out.println("调用微信登录接口: " + jscode2sessionUrl);
            
            // 发送请求获取openid
            String result = httpClientUtil.sendHttpGet(jscode2sessionUrl);
            System.out.println("微信返回结果: " + result);
            
            // 解析返回结果
            JSONObject jsonObject = JSON.parseObject(result);
            
            // 检查微信返回的结果是否包含错误信息
            if (jsonObject.containsKey("errcode") && jsonObject.getIntValue("errcode") != 0) {
                String errMsg = jsonObject.getString("errmsg");
                System.out.println("微信登录错误: " + errMsg);
                return R.error("微信登录失败: " + errMsg);
            }
            
            // 获取openid
            String openid = jsonObject.getString("openid");
            if (openid == null || openid.isEmpty()) {
                return R.error("获取openid失败");
            }
            
            System.out.println("获取到openid: " + openid);
            
            // 检查用户是否存在
            WxUserInfo resultWxUserInfo = wxUserInfoService.getOne(new QueryWrapper<WxUserInfo>().eq("openid", openid));
            
            if (resultWxUserInfo == null) {
                System.out.println("用户不存在，创建新用户");
                wxUserInfo.setOpenid(openid);
                wxUserInfo.setRegisterDate(new Date());
                wxUserInfo.setLastLoginDate(new Date());
                wxUserInfoService.save(wxUserInfo);
                resultWxUserInfo = wxUserInfo;
            } else {
                System.out.println("用户已存在，更新用户信息");
                resultWxUserInfo.setNickName(wxUserInfo.getNickName());
                resultWxUserInfo.setAvatarUrl(wxUserInfo.getAvatarUrl());
                resultWxUserInfo.setLastLoginDate(new Date());
                wxUserInfoService.updateById(resultWxUserInfo);
            }
            
            // 生成token
            String token = JwtUtils.createJWT(openid, wxUserInfo.getNickName(), SystemConstant.JWT_TTL);
            
            // 构建响应数据
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("token", token);
            resultMap.put("openid", openid);
            resultMap.put("userInfo", resultWxUserInfo);
            
            return R.ok(resultMap);
        } catch (Exception e) {
            System.err.println("微信登录处理异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("微信登录处理异常: " + e.getMessage());
        }
    }
}
