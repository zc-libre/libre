package com.libre.framework.system.service.mapstruct;

import com.libre.framework.common.security.dto.RoleInfo;
import com.libre.framework.system.pojo.dto.RoleDTO;
import com.libre.framework.system.pojo.entity.SysRole;
import com.libre.framework.system.pojo.vo.RoleVO;
import com.libre.toolkit.mapstruct.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author Libre
 * @date 2021/7/12 14:48
 */
@Mapper
public interface SysRoleMapping extends BaseMapping<SysRole, RoleInfo> {

	SysRoleMapping INSTANCE = Mappers.getMapper(SysRoleMapping.class);

	List<RoleVO> convertToRoleList(List<SysRole> roles);

	SysRole convertToRole(RoleDTO roleDTO);

	@Override
	@Mapping(source = "roleName", target = "name")
	RoleInfo sourceToTarget(SysRole sysRole);

}
