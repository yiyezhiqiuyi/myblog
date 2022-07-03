package com.myblog.common;

import com.myblog.intercepter.AdminInterceptor;
import com.myblog.intercepter.GlobalIntercepter;
import com.myblog.intercepter.UserInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalConfig implements WebMvcConfigurer {
    /**
     * 全局拦截器
     * @return
     */
    @Bean
    public HandlerInterceptor getGlobalIntercepter() {
        return new GlobalIntercepter();
    }

    /**
     * 让GlobalIntercepter提前加载，否则 在GlobalIntercepter 里面使用 @Autowired 会注入失败
     *
     * @return
     */
    @Bean
    public HandlerInterceptor getAdminInterceptor() {
        return new AdminInterceptor();
    }

    /**
     * 用户拦截器
     * @return
     */
    @Bean
    public HandlerInterceptor getUserInterceptor() {
        return new UserInterceptor();
    }


    /**
     * 添加全局拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //管理员拦截
        registry.addInterceptor(getAdminInterceptor()).addPathPatterns("/myblog/**")
                .excludePathPatterns("/myblog/login", "/myblog/logout", "/myblog/adminLogin");

        //用户拦截器
        registry.addInterceptor(getUserInterceptor()).addPathPatterns("/user/**");

        //用户拦截器
        registry.addInterceptor(getGlobalIntercepter())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**","/css/**","/js/**","/img/**");
    }
}
