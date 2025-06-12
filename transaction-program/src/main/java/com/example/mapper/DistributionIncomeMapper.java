package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.DistributionIncome;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销收益Mapper接口
 */
@Mapper
public interface DistributionIncomeMapper extends BaseMapper<DistributionIncome> {

    /**
     * 查询会员的分销收益列表
     */
    @Select("SELECT * FROM t_distribution_income WHERE member_id = #{memberId} ORDER BY create_time DESC")
    List<DistributionIncome> selectByMemberId(@Param("memberId") Integer memberId);
    
    /**
     * 统计会员待结算收益总额
     */
    @Select("SELECT IFNULL(SUM(income_amount), 0) FROM t_distribution_income WHERE member_id = #{memberId} AND status = 0")
    BigDecimal sumPendingIncome(@Param("memberId") Integer memberId);
    
    /**
     * 统计会员已结算收益总额
     */
    @Select("SELECT IFNULL(SUM(income_amount), 0) FROM t_distribution_income WHERE member_id = #{memberId} AND status = 1")
    BigDecimal sumSettledIncome(@Param("memberId") Integer memberId);
    
    /**
     * 更新收益状态
     */
    @Update("UPDATE t_distribution_income SET status = #{status}, settle_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);
    
    /**
     * 根据订单ID取消相关收益
     */
    @Update("UPDATE t_distribution_income SET status = 2, remark = #{remark} WHERE order_id = #{orderId}")
    int cancelByOrderId(@Param("orderId") Integer orderId, @Param("remark") String remark);
} 