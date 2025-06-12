package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.LinkTrace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 链接追踪Mapper接口
 */
@Mapper
public interface LinkTraceMapper extends BaseMapper<LinkTrace> {

    /**
     * 统计某个链接ID的访问次数
     */
    @Select("SELECT COUNT(*) FROM t_link_trace WHERE link_id = #{linkId}")
    int countByLinkId(@Param("linkId") Integer linkId);
    
    /**
     * 统计某个访问者的访问次数
     */
    @Select("SELECT COUNT(*) FROM t_link_trace WHERE visitor_id = #{visitorId}")
    int countByVisitorId(@Param("visitorId") String visitorId);
} 