package com.example.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品
 */
@TableName("t_product")
@Data
public class Product {

    private Integer id; // 编号

    private String name; // 名称

    private BigDecimal price; // 价格

    private String productIntroImgs; // 商品介绍图片

    private String productParaImgs;  // 商品规格参数图片

    private Integer stock; // 库存

    private String proPic="default.jpg"; // 商品图片

    private boolean isHot=false; // 是否热门推荐商品

    private boolean isSwiper=false; // 是否轮播图片商品

    private Integer swiperSort=0; // 轮播排序

    private String swiperPic="default.jpg"; // 商品轮播图片

    private String description; // 描述


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date hotDateTime; // 设置热门推荐日期时间

    @TableField(select = false)
    private List<ProductSwiperImage> productSwiperImageList;

    @TableField(select = false)
    private SmallType type; // 商品类别

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProductIntroImgs() {
        return productIntroImgs;
    }

    public void setProductIntroImgs(String productIntroImgs) {
        this.productIntroImgs = productIntroImgs;
    }

    public String getProductParaImgs() {
        return productParaImgs;
    }

    public void setProductParaImgs(String productParaImgs) {
        this.productParaImgs = productParaImgs;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getProPic() {
        return proPic;
    }

    public void setProPic(String proPic) {
        this.proPic = proPic;
    }

    public boolean isHot() {
        return isHot;
    }

    public void setHot(boolean hot) {
        isHot = hot;
    }

    public boolean isSwiper() {
        return isSwiper;
    }

    public void setSwiper(boolean swiper) {
        isSwiper = swiper;
    }

    public Integer getSwiperSort() {
        return swiperSort;
    }

    public void setSwiperSort(Integer swiperSort) {
        this.swiperSort = swiperSort;
    }

    public String getSwiperPic() {
        return swiperPic;
    }

    public void setSwiperPic(String swiperPic) {
        this.swiperPic = swiperPic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getHotDateTime() {
        return hotDateTime;
    }

    public void setHotDateTime(Date hotDateTime) {
        this.hotDateTime = hotDateTime;
    }

    public List<ProductSwiperImage> getProductSwiperImageList() {
        return productSwiperImageList;
    }

    public void setProductSwiperImageList(List<ProductSwiperImage> productSwiperImageList) {
        this.productSwiperImageList = productSwiperImageList;
    }

    public SmallType getType() {
        return type;
    }

    public void setType(SmallType type) {
        this.type = type;
    }


}
