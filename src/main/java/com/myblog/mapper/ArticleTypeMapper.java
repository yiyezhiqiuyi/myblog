package com.myblog.mapper;

import com.myblog.vo.ArticleTypeTreeVo;
import com.myblog.vo.ArticleTypeVo;
import com.myblog.entity.ArticleType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleTypeMapper extends BaseMapper<ArticleType> {

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
    List<ArticleTypeTreeVo> getIndexArticleTypeList(@Param("articleTypeParentId") String articleTypeParentId);
}
