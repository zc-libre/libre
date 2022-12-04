package com.zclibre.system.module.system.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 角色菜单表
 * @author zhao.cheng
 */
@ApiModel(value = "角色菜单表")
@Data
@TableName(value = "sys_role_menu")
public class SysRoleMenu {

    @TableId
    private Long id;

    @ApiModelProperty(value = "")
    private Long roleId;

    @ApiModelProperty(value = "")
    private Long menuId;
}
