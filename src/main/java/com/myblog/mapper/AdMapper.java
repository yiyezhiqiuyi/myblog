package com.myblog.mapper;

import com.myblog.entity.Ad;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myblog.vo.AdVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdMapper extends BaseMapper<Ad> {

    /**
     * 广告列表，包含广告类型名称
     * @param adTypeId
     * @return
     */
    List<AdVo> adList(@Param("adTypeId") String adTypeId);
}
