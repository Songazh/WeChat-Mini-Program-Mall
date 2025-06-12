package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.DistributionConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销配置Mapper接口
 */
@Mapper
public interface DistributionConfigMapper extends BaseMapper<DistributionConfig> {

    /**
     * 根据分销等级查询配置
     */
    @Select("SELECT * FROM t_distribution_config WHERE level = #{level}")
    DistributionConfig selectByLevel(@Param("level") Integer level);
    
    /**
     * 查询所有启用的分销配置
     */
    @Select("SELECT * FROM t_distribution_config WHERE status = 1 ORDER BY level ASC")
    List<DistributionConfig> selectAllEnabled();
    
    /**
     * 更新佣金比例
     */
    @Update("UPDATE t_distribution_config SET commission_rate = #{commissionRate}, update_time = NOW() WHERE level = #{level}")
    int updateCommissionRate(@Param("level") Integer level, @Param("commissionRate") BigDecimal commissionRate);
    
    /**
     * 更新积分比例
     */
    @Update("UPDATE t_distribution_config SET point_rate = #{pointRate}, update_time = NOW() WHERE level = #{level}")
    int updatePointRate(@Param("level") Integer level, @Param("pointRate") Integer pointRate);
    
    /**
     * 更新状态
     */
    @Update("UPDATE t_distribution_config SET status = #{status}, update_time = NOW() WHERE level = #{level}")
    int updateStatus(@Param("level") Integer level, @Param("status") Integer status);
} 