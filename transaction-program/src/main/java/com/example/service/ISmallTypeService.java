package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.SmallType;

import java.util.List;
import java.util.Map;

/**
 * 商品小类Service接口
 */
public interface ISmallTypeService extends IService<SmallType> {
    List<SmallType> list(Map<String, Object> map);

    Long getTotal(Map<String, Object> map);
}
