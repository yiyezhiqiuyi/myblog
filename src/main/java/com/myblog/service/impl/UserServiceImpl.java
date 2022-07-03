package com.myblog.service.impl;

import com.myblog.service.IUserService;
import com.myblog.entity.User;
import com.myblog.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
