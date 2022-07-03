package com.myblog.service.impl;

import com.myblog.service.IAdTypeService;
import com.myblog.entity.AdType;
import com.myblog.mapper.AdTypeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AdTypeServiceImpl extends ServiceImpl<AdTypeMapper, AdType> implements IAdTypeService {

}
