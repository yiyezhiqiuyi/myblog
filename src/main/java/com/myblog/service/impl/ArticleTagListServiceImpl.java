package com.myblog.service.impl;

import com.myblog.service.IArticleTagListService;
import com.myblog.entity.ArticleTagList;
import com.myblog.mapper.ArticleTagListMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ArticleTagListServiceImpl extends ServiceImpl<ArticleTagListMapper, ArticleTagList> implements IArticleTagListService {

}
