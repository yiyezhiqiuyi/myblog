package com.myblog.dto.user;

import com.myblog.dto.base.BasePageDto;
import lombok.Data;

@Data
public class UserListPageDto extends BasePageDto {

    /**
     * 用户名
     */
    private String userName;

}
