package com.myblog.service.impl;

import com.myblog.service.IAdService;
import com.myblog.entity.Ad;
import com.myblog.mapper.AdMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myblog.vo.AdVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdServiceImpl extends ServiceImpl<AdMapper, Ad> implements IAdService {
    @Autowired
    private AdMapper adMapper;

    /**
     * 广告列表，包含广告类型名称
     *
     * @param adTypeId
     * @return
     */
    @Override
    public List<AdVo> adList(String adTypeId) {
        return adMapper.adList(adTypeId);
    }
}
