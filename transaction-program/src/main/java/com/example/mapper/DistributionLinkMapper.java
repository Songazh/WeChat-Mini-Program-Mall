package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.DistributionLink;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 分销链接Mapper接口
 */
public interface DistributionLinkMapper extends BaseMapper<DistributionLink> {

    /**
     * 根据链接编码查询分销链接
     */
    @Select("SELECT * FROM t_distribution_link WHERE link_code = #{linkCode}")
    DistributionLink selectByLinkCode(@Param("linkCode") String linkCode);
    
    /**
     * 统计链接编码数量
     */
    @Select("SELECT COUNT(*) FROM t_distribution_link WHERE link_code = #{linkCode}")
    int countByLinkCode(@Param("linkCode") String linkCode);
} 