package com.libre.framework.system.pojo.dto;

import com.libre.framework.common.base.BaseCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Libre
 * @date 2022/2/2 19:16
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeptCriteria extends BaseCriteria {

	private Long parentId;

}
