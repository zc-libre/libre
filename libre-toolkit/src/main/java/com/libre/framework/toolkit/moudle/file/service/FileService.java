package com.libre.framework.toolkit.moudle.file.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.libre.framework.toolkit.moudle.file.pojo.SysFile;
import com.libre.framework.toolkit.moudle.file.pojo.SysFileCriteria;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Libre
 * @Date: 2023/1/27 4:09 AM
 */
public interface FileService extends IService<SysFile> {

	PageDTO<SysFile> findByPage(PageDTO<SysFile> page, SysFileCriteria criteria);

	String createFile(MultipartFile file, Integer saveType);

	String getFilePath(Long id);

	void deleteFile(Long fileId);

	void sync(Integer saveType);

}