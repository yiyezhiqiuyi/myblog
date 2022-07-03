package com.myblog.service.impl;

import com.myblog.service.IArticleTagService;
import com.myblog.entity.ArticleTag;
import com.myblog.mapper.ArticleTagMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements IArticleTagService {

}
