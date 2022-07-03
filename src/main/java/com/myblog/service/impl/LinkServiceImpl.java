package com.myblog.service.impl;

import com.myblog.service.ILinkService;
import com.myblog.entity.Link;
import com.myblog.mapper.LinkMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LinkServiceImpl extends ServiceImpl<LinkMapper, Link> implements ILinkService {

}
