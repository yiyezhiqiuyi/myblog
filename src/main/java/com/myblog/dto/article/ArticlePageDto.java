package com.myblog.dto.article;

import com.myblog.dto.base.BasePageDto;
import lombok.Data;

@Data
public class ArticlePageDto extends BasePageDto {

    /**
     * 文章标题
     */
    private String articleTitle;

}
