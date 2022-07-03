package com.myblog.service.impl;

import com.myblog.service.IAdminService;
import com.myblog.entity.Admin;
import com.myblog.mapper.AdminMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

}
