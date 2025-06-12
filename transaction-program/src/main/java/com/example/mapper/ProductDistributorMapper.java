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

    /**
     * 根据商品ID和购买者openId查询其推荐人的分销资格
     * 用于查找二级分销者
     */
    @Select("SELECT pd.* FROM t_product_distributor pd " +
            "JOIN t_member m ON pd.member_id = m.id " +
            "WHERE pd.product_id = #{productId} AND pd.status = 1 " +
            "AND m.open_id = #{buyerOpenId}")
    ProductDistributor selectByProductAndBuyer(@Param("productId") Integer productId, @Param("buyerOpenId") String buyerOpenId);
} 