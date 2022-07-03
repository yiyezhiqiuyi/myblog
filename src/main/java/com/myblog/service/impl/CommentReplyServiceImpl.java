package com.myblog.service.impl;

import com.myblog.service.ICommentReplyService;
import com.myblog.entity.CommentReply;
import com.myblog.mapper.CommentReplyMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CommentReplyServiceImpl extends ServiceImpl<CommentReplyMapper, CommentReply> implements ICommentReplyService {

}
