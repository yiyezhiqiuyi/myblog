package com.myblog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.dto.article.PublishArticleActionDto;
import com.myblog.entity.Article;
import com.baomidou.mybatisplus.extension.service.IService;
import com.myblog.entity.User;
import com.myblog.utils.CommonResult;
import com.myblog.vo.ArticleVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IArticleService extends IService<Article> {

    /**
     * 文章列表
     * @param articlePage
     * @param articleTitle
     * @return
     */
    IPage<ArticleVo> articleList(IPage<ArticleVo> articlePage, String articleTitle,String userId);

    /**
     * 文章列表 前端
     * @param articlePage
     * @param articleTitle
     * @param articleTypeId
     * @return
     */
    IPage<ArticleVo> articleListView(Page<ArticleVo> articlePage, String articleTitle, String articleTypeId);

    /**
     * 发布文章方法
     * @param publishArticleActionDto
     * @return
     */
    CommonResult publishArticleAction(HttpServletRequest request, PublishArticleActionDto publishArticleActionDto);

    /**
     * 删除文章
     * @param articleId
     * @return
     */
    CommonResult delArticle(String articleId);

    /**
     * 首页最新文章
     * @return
     */
    List<ArticleVo> getIndexArticleList();

    /**
     * 根据文章id获取文章信息
     * @param articleId
     * @return
     */
    ArticleVo getArticle(String articleId);

    /**
     * 收藏文章
     * @param user
     * @param articleId
     * @return
     */
    CommonResult articleCollection(User user, String articleId);

    /**
     * 获取标签对应的文章列表
     * @param articlePage
     * @param articleTagId
     * @return
     */
    IPage<ArticleVo> tagArticleList(Page<ArticleVo> articlePage, String articleTagId);

}
