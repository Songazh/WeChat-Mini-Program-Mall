package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Member;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会员Mapper接口
 */
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 根据用户ID查询会员
     */
    @Select("SELECT * FROM t_member WHERE user_id = #{userId}")
    Member selectByUserId(@Param("userId") String userId);
} 