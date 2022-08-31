package com.zclibre.system.module.system.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * 前端菜单路由元数据
 *
 */
@Getter
@RequiredArgsConstructor
public class MenuMetaVO implements Serializable {

	private final String title;

	private final String icon;

	private final Integer noCache;

}
