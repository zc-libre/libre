package com.libre.framework.system.service.mapstruct;

import com.libre.framework.common.mapstruct.BooleanAndIntegerFormat;
import com.libre.framework.system.pojo.dto.MenuDTO;
import com.libre.framework.system.pojo.entity.SysMenu;
import com.libre.toolkit.mapstruct.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author: Libre
 * @Date: 2023/1/8 1:38 AM
 */
@Mapper(uses = BooleanAndIntegerFormat.class)
public interface SysMenuMapping extends BaseMapping<MenuDTO, SysMenu> {

	SysMenuMapping INSTANCE = Mappers.getMapper(SysMenuMapping.class);

	@Override
	SysMenu sourceToTarget(MenuDTO menuDTO);

}
