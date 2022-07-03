package com.myblog.entity;

import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 友情连接id
     */
    @TableId(value = "link_id")
    private String linkId;

    /**
     * 友情连接标题
     */
    private String linkTitle;

    /**
     * 友情连接的地址
     */
    private String linkUrl;

    /**
     * 友情连接logo
     */
    private String linkLogoUrl;

    /**
     * 友情排序
     */
    private Integer linkSort;

    /**
     * 添加友情连接的时间
     */
    private Date linkAddTime;


}
