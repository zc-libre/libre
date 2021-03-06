package com.libre.admin.project.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.libre.admin.common.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位表
 * @author zhao.cheng
 */
@ApiModel(value = "岗位表")
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_post")
public class Post extends BaseEntity {

    /**
     * 部门名称
     */

    @ApiModelProperty(value = "部门名称")
    private String postName;

    /**
     * 排序
     */

    @ApiModelProperty(value = "排序")
    private String sort;

    /**
     * 状态
     */

    @ApiModelProperty(value = "状态")
    private Integer status;


}
