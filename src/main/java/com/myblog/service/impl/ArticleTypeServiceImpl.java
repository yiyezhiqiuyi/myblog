package com.myblog.service.impl;

import com.myblog.service.IArticleTypeService;
import com.myblog.entity.ArticleType;
import com.myblog.mapper.ArticleTypeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myblog.vo.ArticleTypeTreeVo;
import com.myblog.vo.ArticleTypeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleTypeServiceImpl extends ServiceImpl<ArticleTypeMapper, ArticleType> implements IArticleTypeService {
    @Autowired
    private ArticleTypeMapper articleTypeMapper;
    /**
     * 文章类型列表，包含文章数量
     * @return
     */
    @Override
    public List<ArticleTypeVo> articleTypeList() {
        return articleTypeMapper.articleTypeList();
    }

    /**
     * 获取首页文章类型树形目录
     * @param articleTypeParentId
     * @return
     */
    @Override
    public List<ArticleTypeTreeVo> getIndexArticleTypeList(String articleTypeParentId) {
        return articleTypeMapper.getIndexArticleTypeList(articleTypeParentId);
    }
}
