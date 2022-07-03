package com.myblog.service;

import com.myblog.entity.UploadFileList;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadFileListService extends IService<UploadFileList> {

    /**
     * 文件上传后获取文件路径
     * @param file
     * @return
     */
    String getUploadFileUrl(MultipartFile file);
}
