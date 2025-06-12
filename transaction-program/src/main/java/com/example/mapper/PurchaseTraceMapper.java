package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.PurchaseTrace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 购买追踪Mapper接口
 */
@Mapper
public interface PurchaseTraceMapper extends BaseMapper<PurchaseTrace> {

    /**
     * 根据订单ID查询购买追踪
     */
    @Select("SELECT * FROM t_purchase_trace WHERE order_id = #{orderId}")
    PurchaseTrace selectByOrderId(@Param("orderId") Integer orderId);
    
    /**
     * 根据链接码查询购买追踪记录数
     */
    @Select("SELECT COUNT(*) FROM t_purchase_trace WHERE link_code = #{linkCode}")
    int countByLinkCode(@Param("linkCode") String linkCode);
    
    /**
     * 根据购买者ID统计购买次数
     */
    @Select("SELECT COUNT(*) FROM t_purchase_trace WHERE buyer_id = #{buyerId}")
    int countByBuyerId(@Param("buyerId") String buyerId);
} 