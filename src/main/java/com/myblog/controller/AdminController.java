package com.myblog.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.system.HostInfo;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.dto.ad.AdDto;
import com.myblog.dto.article.ArticlePageDto;
import com.myblog.dto.article.ArticleTypeUpdateDto;
import com.myblog.dto.user.UserDto;
import com.myblog.dto.user.UserListPageDto;
import com.myblog.entity.*;
import com.myblog.service.*;
import com.myblog.utils.CommonPage;
import com.myblog.utils.CommonResult;
import com.myblog.vo.AdVo;
import com.myblog.vo.ArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/myblog")
@Slf4j
public class AdminController {
    @Autowired
    private IAdminService adminService;
    @Autowired
    private IArticleTypeService articleTypeService;
    @Autowired
    private IArticleTagService articleTagService;
    @Autowired
    private IArticleTagListService articleTagListService;
    @Autowired
    private IArticleService articleService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ILinkService linkService;
    @Autowired
    private IAdTypeService adTypeService;
    @Autowired
    private IAdService adService;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private IUploadFileListService uploadFileListService;
    @Autowired
    private ICommentReplyService commentReplyService;
    @Autowired
    private ICommentService commentService;


    /**
     * 登录页面
     *
     * @return
     */
    @GetMapping("/login")
    public String adminLogin(HttpServletRequest request) {
        if (Objects.nonNull(request.getSession().getAttribute("admin"))) {
            return "redirect:/myblog/";
        }
        return "/admin/adminLogin";
    }

    /**
     * 管理员登录
     *
     * @param request
     * @param adminName
     * @param adminPassword
     * @param verifyCode
     * @return
     */
    @PostMapping("/adminLogin")
    @ResponseBody
    public CommonResult adminLogin(HttpServletRequest request,
                                   String adminName,
                                   String adminPassword,
                                   String verifyCode) {
        HttpSession session = request.getSession();
        if (StrUtil.isBlank(verifyCode) || !verifyCode.equals(session.getAttribute("circleCaptchaCode"))) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("验证码不正确");
        }
        Admin admin = adminService.getOne(Wrappers.<Admin>lambdaQuery()
                .eq(Admin::getAdminName, adminName)
                .eq(Admin::getAdminPassword, SecureUtil.md5(adminName + adminPassword)), false);
        if (Objects.isNull(admin)) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("用户名或者密码不正确");
        }
        session.setAttribute("admin", admin);
        return CommonResult.success("登录成功");
    }


    /**
     * 管理员退出登录
     *
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("admin");
        return "redirect:/myblog/login";
    }


    /**
     * 管理端 - 首页
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String adminIndex(Model model) {

        //系统信息
        OsInfo osInfo = SystemUtil.getOsInfo();
        HostInfo hostInfo = SystemUtil.getHostInfo();
        model.addAttribute("osName", osInfo.getName());
        model.addAttribute("hostAddress", hostInfo.getAddress());

        //文章数量
        int articleTypeCount = articleTypeService.count();
        int articleTagCount = articleTagListService.count();
        int articleCount = articleService.count();
        model.addAttribute("articleTypeCount", articleTypeCount);
        model.addAttribute("articleTagCount", articleTagCount);
        model.addAttribute("articleCount", articleCount);

        //用户数量
        int userCount = userService.count();
        model.addAttribute("userCount", userCount);

        return "/admin/adminIndex";
    }


    /**
     * 管理端 - 用户列表
     *
     * @param userListPageDto
     * @param model
     * @return
     */
    @GetMapping("/user/list")
    public String userList(@Valid UserListPageDto userListPageDto, Model model) {
        Integer pageNumber = userListPageDto.getPageNumber();
        String userName = userListPageDto.getUserName();


        IPage<User> userPage = new Page<>(pageNumber, 24);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = Wrappers.<User>lambdaQuery().orderByDesc(User::getUserRegisterTime);
        if (StrUtil.isNotBlank(userName)) {
            userLambdaQueryWrapper.like(User::getUserName, userName);
            model.addAttribute("userName", userName);
        }

        IPage<User> userIPage = userService.page(userPage, userLambdaQueryWrapper);
        model.addAttribute("userPage", CommonPage.restPage(userIPage));

        return "/admin/userList";
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @PostMapping("/user/del")
    @ResponseBody
    public CommonResult userDel(String userId) {
        if (StrUtil.isBlank(userId)) {
            return CommonResult.failed("参数错误，请刷新页面重试");
        }
        if (articleService.count(Wrappers.<Article>lambdaQuery().eq(Article::getUserId, userId)) > 0) {
            return CommonResult.failed("该用户发布过文章，无法删除，请冻结用户");
        }

        if (userService.removeById(userId)) {
            return CommonResult.success("删除成功");
        }

        return CommonResult.failed("删除失败");
    }


    /**
     * 用户修改
     *
     * @param userDto
     * @return
     */
    @PostMapping("/user/update")
    @ResponseBody
    public CommonResult userUpdate(@Valid UserDto userDto) {
        User user = userService.getById(userDto.getUserId());
        if (Objects.isNull(user)) {
            return CommonResult.failed("用户id 不正确");
        }
        Date userRegisterTime = user.getUserRegisterTime();
        String userPassword = userDto.getUserPassword();
        if (StrUtil.isNotBlank(userPassword)) {
            //用户密码 = md5（注册时间 + 用户明文密码)
            userDto.setUserPassword(SecureUtil.md5(userRegisterTime + userPassword));
        } else {
            userDto.setUserPassword(null);
        }
        BeanUtils.copyProperties(userDto, user);


        if (userService.updateById(user)) {
            return CommonResult.success("修改成功");
        }

        return CommonResult.failed("修改失败,请重试");
    }

    /**
     * 文章类型列表，包含文章数量
     *
     * @return
     */
    @GetMapping("/article/type/list")
    public String articleTypeList(Model model, String articleTypeParentId) {
        List<ArticleType> articleType0List = articleTypeService.list(Wrappers.<ArticleType>lambdaQuery().isNull(ArticleType::getArticleTypeParentId).or().eq(ArticleType::getArticleTypeParentId, "").orderByAsc(ArticleType::getArticleTypeSort));
        LambdaQueryWrapper<ArticleType> queryWrapper = Wrappers.<ArticleType>lambdaQuery()
                .isNotNull(ArticleType::getArticleTypeParentId)
                .ne(ArticleType::getArticleTypeParentId,"")
                .orderByAsc(ArticleType::getArticleTypeSort);
        if (StrUtil.isNotBlank(articleTypeParentId)) {
            queryWrapper.eq(ArticleType::getArticleTypeParentId, articleTypeParentId);
            model.addAttribute("articleTypeName", articleTypeService.getById(articleTypeParentId).getArticleTypeName());
        }
        List<ArticleType> articleType1List = articleTypeService.list(queryWrapper);


        model.addAttribute("articleType0List", articleType0List);
        model.addAttribute("articleType1List", articleType1List);
        return "/admin/articleTypeList";
    }


    /**
     * 添加文章类型
     *
     * @param articleType
     * @return
     */
    @PostMapping("/article/type/addOrUpdate")
    @ResponseBody
    public CommonResult articleTypeAdd(@Valid ArticleType articleType) {
        servletContext.removeAttribute("articleTypeList");
        String articleTypeId = articleType.getArticleTypeId();
        if(StrUtil.isNotBlank(articleType.getArticleTypeParentId()) && StrUtil.isNotBlank(articleType.getArticleTypeId()) && articleType.getArticleTypeParentId().equals(articleType.getArticleTypeId())){
            return CommonResult.failed("不能将自己分配到自己的目录下");
        }

        if (StrUtil.isBlank(articleTypeId)) {
            articleType.setArticleTypeAddTime(DateUtil.date());
            if (articleTypeService.save(articleType)) {

                return CommonResult.success("添加成功");
            }
        }
        if (articleTypeService.updateById(articleType)) {
            return CommonResult.success("修改成功");
        }

        return CommonResult.failed("操作失败");
    }


    /**
     * 修改文章类型
     *
     * @param articleTypeUpdateDto
     * @return
     */
    @PostMapping("/article/type/update")
    @ResponseBody
    public CommonResult articleTypeUpdate(@Valid ArticleTypeUpdateDto articleTypeUpdateDto) {
        ArticleType articleType = new ArticleType();
        BeanUtils.copyProperties(articleTypeUpdateDto, articleType);

        String articleTypeName = articleType.getArticleTypeName();
        Integer articleTypeSort = articleType.getArticleTypeSort();
        if (StrUtil.isBlank(articleTypeName)) {
            articleType.setArticleTypeName(null);
        }
        if (Objects.isNull(articleTypeSort)) {
            articleType.setArticleTypeSort(null);
        }
        if(StrUtil.isNotBlank(articleType.getArticleTypeParentId()) && StrUtil.isNotBlank(articleType.getArticleTypeId()) && articleType.getArticleTypeParentId().equals(articleType.getArticleTypeId())){
            return CommonResult.failed("不能将自己分配到自己的目录下");
        }

        if (articleTypeService.updateById(articleType)) {
            servletContext.removeAttribute("articleTypeList");
            return CommonResult.success("添加成功");
        }
        return CommonResult.failed("添加失败");
    }


    /**
     * 删除文章分类
     *
     * @param articleTypeId
     * @return
     */
    @PostMapping("/article/type/del")
    @ResponseBody
    public CommonResult articleTypeDel(@NotBlank(message = "文章分类id 不能为空") String articleTypeId) {
        if (articleService.count(Wrappers.<Article>lambdaQuery()
                .eq(Article::getArticleTypeId, articleTypeId)) > 0) {
            return CommonResult.failed("请先删除该分类下的文章");
        }

        if (articleTypeService.count(Wrappers.<ArticleType>lambdaQuery().eq(ArticleType::getArticleTypeParentId, articleTypeId)) > 0) {
            return CommonResult.failed("请先删除下级分类");
        }

        if (articleTypeService.removeById(articleTypeId)) {
            servletContext.removeAttribute("articleTypeList");
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }


    /**
     * 文章标签列
     *
     * @param model
     * @return
     */
    @GetMapping("/article/tag/list")
    public String articleTagList(Model model) {
        List<ArticleTag> articleTagList = articleTagService.list(Wrappers.<ArticleTag>lambdaQuery().orderByDesc(ArticleTag::getArticleTagAddTime));
        model.addAttribute("articleTagList", articleTagList);
        return "/admin/articleTagList";
    }

    /**
     * 文章标签 添加
     *
     * @param articleTag
     * @return
     */
    @PostMapping("/article/tag/addOrUpdate")
    @ResponseBody
    public CommonResult articleTagAddOrUpdate(ArticleTag articleTag) {
        servletContext.removeAttribute("articleTagList");
        String articleTagId = articleTag.getArticleTagId();
        if (StrUtil.isNotBlank(articleTagId)) {
            if (articleTagService.updateById(articleTag)) {
                return CommonResult.success("修改成功");
            }
            return CommonResult.failed("修改失败");
        }


        articleTag.setArticleTagAddTime(DateUtil.date());
        if (articleTagService.save(articleTag)) {
            return CommonResult.success("文章标签添加成功");
        }
        return CommonResult.failed("文章标签添加失败");
    }

    /**
     * 文章标签 删除
     *
     * @param articleTagId
     * @return
     */
    @PostMapping("/article/tag/del")
    @ResponseBody
    public CommonResult articleTagDel(String articleTagId) {
        if (StrUtil.isBlank(articleTagId)) {
            return CommonResult.failed("删除失败，没有获取到文章标签id");
        }

        if (articleTagListService.count(Wrappers.<ArticleTagList>lambdaQuery()
                .eq(ArticleTagList::getArticleTagId, articleTagId)) > 0) {
            return CommonResult.failed("该问标签已经被使用，无法删除，请先删除和文章的关联");
        }

        if (articleTagService.removeById(articleTagId)) {
            servletContext.removeAttribute("articleTagList");
            return CommonResult.success("删除标签成功");
        }
        return CommonResult.failed("删除标签失败");
    }

    /**
     * 文章列表
     *
     * @param articlePageDto
     * @return
     */
    @GetMapping("/article/list")
    public String articleList(@Valid ArticlePageDto articlePageDto, Model model) {
        IPage<ArticleVo> articleVoPage = new Page<>(articlePageDto.getPageNumber(), 24);
        IPage<ArticleVo> articleVoIPage = articleService.articleList(articleVoPage, articlePageDto.getArticleTitle(),null);
        model.addAttribute("articleVoIPage", CommonPage.restPage(articleVoIPage));
        if (StrUtil.isNotBlank(articlePageDto.getArticleTitle())) {
            model.addAttribute("articleTitle", articlePageDto.getArticleTitle());
        }
        return "/admin/articleList";
    }

    /**
     * 设置为热门文章
     *
     * @param articleId
     * @return
     */
    @PostMapping("/article/hot")
    @ResponseBody
    public CommonResult articleHot(String articleId) {
        if (articleService.update(Wrappers.<Article>lambdaUpdate().eq(Article::getArticleId,articleId).set(Article::getArticleHot,1))) {
            servletContext.removeAttribute("articleHotList");
            return CommonResult.success("设置成功");
        }
        return CommonResult.failed("设置失败");
    }

    /**
     * 文章删除
     *
     * @param articleId
     * @return
     */
    @PostMapping("/article/del")
    @ResponseBody
    public CommonResult articleDel(String articleId) {
        return articleService.delArticle(articleId);
    }

    /**
     * 友情连接列表页面
     *
     * @return
     */
    @GetMapping("/link/list")
    public String linkList(Model model) {
        List<Link> linkList = linkService.list(Wrappers.<Link>lambdaQuery().orderByAsc(Link::getLinkSort));
        model.addAttribute("linkList", linkList);
        return "/admin/linkList";
    }


    /**
     * 更新友联
     *
     * @param link
     * @return
     */
    @PostMapping("/link/addOrUpdate")
    @ResponseBody
    public CommonResult linkAddOrUpdate(Link link) {
        servletContext.removeAttribute("linkList");
        String linkId = link.getLinkId();
        if (StrUtil.isBlank(linkId)) {
            //添加友联
            link.setLinkAddTime(DateUtil.date());
            if (linkService.save(link)) {
                return CommonResult.success("添加成功");
            }
            return CommonResult.failed("添加失败");
        }

        if (linkService.updateById(link)) {
            return CommonResult.success("更新成功");
        }
        return CommonResult.failed("更新失败");

    }

    /**
     * 删除友联
     *
     * @param linkId
     * @return
     */
    @PostMapping("/link/del")
    @ResponseBody
    public CommonResult linkDel(String linkId) {
        if (linkService.removeById(linkId)) {
            servletContext.removeAttribute("linkList");
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 广告管理
     *
     * @param model
     * @return
     */
    @GetMapping("/ad/list")
    public String adList(String adTypeId, Model model) {
        List<AdType> adTypeList = adTypeService.list(Wrappers.<AdType>lambdaQuery()
                .orderByAsc(AdType::getAdTypeSort));
        model.addAttribute("adTypeList", adTypeList);

        List<AdVo> adVoList = adService.adList(adTypeId);
        model.addAttribute("adVoList", adVoList);

        return "/admin/adList";
    }


    /**
     * 广告类型管理
     *
     * @param adType
     * @return
     */
    @PostMapping("/ad/type/addOrUpdate")
    @ResponseBody
    public CommonResult adTypeAddOrUpdate(AdType adType) {
        String adTypeId = adType.getAdTypeId();
        if (StrUtil.isBlank(adTypeId)) {
            //添加广告类型
            adType.setAdTypeAddTime(DateUtil.date());
            if (adTypeService.save(adType)) {
                return CommonResult.success("添加成功");
            }
            return CommonResult.success("添加失败");
        }

        //修改广告类型
        if (adTypeService.updateById(adType)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }


    /**
     * 广告管理
     *
     * @param adDto
     * @return
     */
    @PostMapping("/ad/addOrUpdate")
    @ResponseBody
    public CommonResult adAddOrUpdate(AdDto adDto, MultipartFile file) throws IOException {
        if (Objects.nonNull(file)) {
            adDto.setAdImgUrl(uploadFileListService.getUploadFileUrl(file));
        }

        String adId = adDto.getAdId();
        Ad ad = new Ad();
        BeanUtils.copyProperties(adDto, ad);
        ad.setAdBeginTime(DateUtil.parseDateTime(adDto.getAdBeginTime()));
        ad.setAdEndTime(DateUtil.parseDateTime(adDto.getAdEndTime()));

        //移除首页广告缓存
        servletContext.removeAttribute("adIndexList");
        servletContext.removeAttribute("adArticleList");

        if (StrUtil.isBlank(adId)) {
            //添加广告类型
            ad.setAdAddTime(DateUtil.date());
            if (adService.save(ad)) {
                return CommonResult.success("添加成功");
            }
            return CommonResult.success("添加失败");
        }

        //修改广告类型
        if (adService.updateById(ad)) {
            return CommonResult.success("修改成功");
        }
        return CommonResult.failed("修改失败");
    }

    /**
     * 删除广告
     *
     * @param adId
     * @return
     */
    @PostMapping("/ad/del")
    @ResponseBody
    public CommonResult adDel(String adId) {
        if (adService.removeById(adId)) {
            servletContext.removeAttribute("adIndexList");
            servletContext.removeAttribute("adArticleList");
            return CommonResult.success("删除成功");
        }
        return CommonResult.failed("删除失败");
    }

    /**
     * 修改admin密码
     *
     * @param newPassword
     * @return
     */
    @PostMapping("/password/update")
    @ResponseBody
    public CommonResult passwordUpdate(HttpServletRequest request, String newPassword) {
        if (StrUtil.isNotBlank(newPassword)) {
            Admin admin = adminService.getOne(null, false);
            if (Objects.nonNull(admin)) {
                admin.setAdminPassword(SecureUtil.md5(admin.getAdminName() + newPassword));
                if (adminService.updateById(admin)) {
                    request.getSession().setAttribute("admin", admin);
                    return CommonResult.success("修改成功");
                }
            }
        }
        return CommonResult.failed("修改失败");
    }


}
