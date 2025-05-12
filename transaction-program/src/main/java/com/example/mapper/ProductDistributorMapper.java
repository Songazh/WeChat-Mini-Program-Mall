package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ProductDistributor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 商品分销者Mapper接口
 */
public interface ProductDistributorMapper extends BaseMapper<ProductDistributor> {

    /**
     * 根据会员ID和商品ID查询分销资格
     */
    @Select("SELECT * FROM t_product_distributor WHERE member_id = #{memberId} AND product_id = #{productId}")
    ProductDistributor selectByMemberAndProduct(@Param("memberId") Integer memberId, @Param("productId") Integer productId);
} 