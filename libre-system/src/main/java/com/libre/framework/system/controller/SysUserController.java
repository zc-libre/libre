package com.libre.framework.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.libre.framework.common.security.constant.SecurityConstant;
import com.libre.framework.toolkit.moudle.log.annotation.ApiLog;
import com.libre.framework.system.pojo.dto.UserCriteria;
import com.libre.framework.system.pojo.dto.UserDTO;
import com.libre.framework.system.pojo.entity.SysUser;
import com.libre.framework.system.pojo.vo.UserVO;
import com.libre.framework.system.service.SysUserService;
import com.libre.toolkit.core.StringPool;
import com.libre.toolkit.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * @author zhao.cheng
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class SysUserController {

	private final SysUserService sysUserService;

	private final PasswordEncoder passwordEncoder;

	@ApiOperation("用户列表")
	@GetMapping("/page")
	public R<PageDTO<UserVO>> page(Page<SysUser> page, UserCriteria param) {
		PageDTO<UserVO> userPage = sysUserService.findByPage(page, param);
		return R.data(userPage);
	}

	@ApiOperation("获取用户")
	@GetMapping("/get/{id}")
	public R<SysUser> info(@PathVariable Long id) {
		return R.data(sysUserService.findUserById(id));
	}

	@ApiLog("创建用户")
	@PutMapping("/save")
	public R<Boolean> save(@RequestBody UserDTO user) {
		String password = passwordEncoder.encode("123456");
		user.setPassword(password.replace(SecurityConstant.BCRYPT, StringPool.EMPTY));
		boolean res = sysUserService.createUser(user);
		return R.status(res);
	}

	@ApiLog("修改用户")
	@PostMapping("edit")
	public R<Boolean> update(@RequestBody UserDTO user) {
		boolean res = sysUserService.updateUser(user);
		return R.status(res);
	}

	@ApiLog("删除用户")
	@DeleteMapping
	public R<Boolean> delete(@NotEmpty @RequestBody Set<Long> ids) {
		boolean res = sysUserService.deleteUserByIds(ids);
		return R.status(res);
	}

}
