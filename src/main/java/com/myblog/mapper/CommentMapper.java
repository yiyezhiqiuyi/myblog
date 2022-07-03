package com.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.vo.CommentVo;
import com.myblog.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 文章评论列表
     * @param articleId
     * @return
     */
    IPage<CommentVo> getArticleCommentList(Page<CommentVo> commentVoPage, @Param("articleId") String articleId);
}
