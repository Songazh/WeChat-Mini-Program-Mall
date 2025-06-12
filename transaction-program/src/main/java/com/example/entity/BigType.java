package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品大类
 */
@TableName("t_bigtype")
@Data
public class BigType implements Serializable {

    private Integer id; // 编号

    private String name; // 名称

    private String remark; // 备注

    private String image="default.jpg"; // 封面图片

    @TableField(select = false)
    private List<SmallType> smallTypeList; // 商品小类集合

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<SmallType> getSmallTypeList() {
        return smallTypeList;
    }

    public void setSmallTypeList(List<SmallType> smallTypeList) {
        this.smallTypeList = smallTypeList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
