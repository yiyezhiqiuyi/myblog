package com.myblog.dto.user;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserDto {
    /**
     * 用户id
     */
    @NotBlank(message = "用户id 不能为空")
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 是否冻结，0正常，1冻结（冻结后无法登陆）
     */
    private Integer userFrozen;

    /**
     * 是否可以发布文章 0不能，1可以发布
     */
    private Integer userPublishArticle;

}
