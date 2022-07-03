package com.myblog.service;

import com.myblog.entity.ArticleType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myblog.vo.ArticleTypeTreeVo;
import com.myblog.vo.ArticleTypeVo;

import java.util.List;

public interface IArticleTypeService extends IService<ArticleType> {

    /**
     * 文章类型列表，包含文章数量
     * @return
     */
    List<ArticleTypeVo> articleTypeList();

    /**
     * 获取首页文章类型树形目录
     * @param articleTypeParentId
     * @return
     */
    List<ArticleTypeTreeVo> getIndexArticleTypeList(String articleTypeParentId);
}
