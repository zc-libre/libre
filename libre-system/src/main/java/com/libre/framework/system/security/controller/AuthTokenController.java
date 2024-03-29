package com.libre.framework.system.security.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.libre.framework.system.security.jwt.JwtTokenStore;
import com.libre.framework.system.security.pojo.vo.TokenVo;
import com.libre.toolkit.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

/**
 * 认证 token 管理
 *
 * @author L.cm
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
@Tag(name = "系统：token管理")
public class AuthTokenController {

	private final JwtTokenStore tokenStore;

	@Operation(summary = "查询列表")
	@GetMapping
	@PreAuthorize("@sec.isAuthenticated()")
	public R<Page<TokenVo>> query(PageDTO<TokenVo> page, String filter) {
		return R.data(tokenStore.page(page, filter));
	}

	@Operation(summary = "踢出用户")
	@DeleteMapping
	@PreAuthorize("@sec.isAdmin()")
	public R<Boolean> delete(@NotEmpty @RequestBody Set<String> keys) {
		tokenStore.remove(keys);
		return R.status(true);
	}

}
