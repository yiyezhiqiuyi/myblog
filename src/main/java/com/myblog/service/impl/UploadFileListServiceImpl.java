package com.myblog.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myblog.service.IUploadFileListService;
import com.myblog.entity.UploadFileList;
import com.myblog.mapper.UploadFileListMapper;
import com.myblog.utils.CommonUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

@Service
public class UploadFileListServiceImpl extends ServiceImpl<UploadFileListMapper, UploadFileList> implements IUploadFileListService {

    /**
     * 文件上传后获取文件路径
     *
     * @param file
     * @return
     */
    @Override
    public String getUploadFileUrl(MultipartFile file) {

        if (file.isEmpty() || file.getSize() < 1) {
            return null;
        }
        long size = file.getSize();
        //查询该图片是否已经上传过了
        UploadFileList uploadFileList = getOne(Wrappers.<UploadFileList>lambdaQuery().eq(UploadFileList::getFileSize, size).orderByDesc(UploadFileList::getUploadFileTime), false);
        if (Objects.nonNull(uploadFileList)) {
            File uploadFile = new File(CommonUtils.getClasspath(), "static" + uploadFileList.getFileUrl());
            if (FileUtil.exist(uploadFile)) {
                return uploadFileList.getFileUrl();
            }
        }


        //获取文件名
        String fileName = file.getOriginalFilename();
        fileName = CommonUtils.getFileName(fileName);
        String filepath = CommonUtils.getUploadPath();
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filepath + File.separator + fileName))) {
            out.write(file.getBytes());
            out.flush();
            //将该图片保存到数据库，下次直接获取数据库里面的文件url
            uploadFileList = new UploadFileList();
            uploadFileList.setFileUrl(CommonUtils.getUploadFilePath() + fileName);
            uploadFileList.setUploadFileTime(DateUtil.date());
            uploadFileList.setFileSize(size);
            save(uploadFileList);
            return uploadFileList.getFileUrl();
        } catch (Exception e) {
            log.error("上传图片失败：" + e.getMessage());
        }
        return null;
    }
}
