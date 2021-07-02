package com.libre.system.service;

import com.libre.system.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author zhao.cheng
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 通过id查找用户
     *
     * @param id /
     * @return /
     */
    SysUser findUserById(Long id);
}
