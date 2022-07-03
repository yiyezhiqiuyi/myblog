package com.myblog.service.impl;

import com.myblog.service.IUserCollectionArticleService;
import com.myblog.entity.UserCollectionArticle;
import com.myblog.mapper.UserCollectionArticleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserCollectionArticleServiceImpl extends ServiceImpl<UserCollectionArticleMapper, UserCollectionArticle> implements IUserCollectionArticleService {

}
