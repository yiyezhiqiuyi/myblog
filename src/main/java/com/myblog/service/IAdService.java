package com.myblog.service;

import com.myblog.entity.Ad;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myblog.vo.AdVo;

import java.util.List;

public interface IAdService extends IService<Ad> {

    /**
     * 广告列表，包含广告类型名称
     * @param adTypeId
     * @return
     */
    List<AdVo> adList(String adTypeId);
}
