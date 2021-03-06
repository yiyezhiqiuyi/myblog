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
     * ????????????
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
     * ???????????????
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
            return CommonResult.failed("??????????????????");
        }
        Admin admin = adminService.getOne(Wrappers.<Admin>lambdaQuery()
                .eq(Admin::getAdminName, adminName)
                .eq(Admin::getAdminPassword, SecureUtil.md5(adminName + adminPassword)), false);
        if (Objects.isNull(admin)) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("??????????????????????????????");
        }
        session.setAttribute("admin", admin);
        return CommonResult.success("????????????");
    }


    /**
     * ?????????????????????
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
     * ????????? - ??????
     *
     * @param model
     * @return
     */
    @GetMapping("/")
    public String adminIndex(Model model) {

        //????????????
        OsInfo osInfo = SystemUtil.getOsInfo();
        HostInfo hostInfo = SystemUtil.getHostInfo();
        model.addAttribute("osName", osInfo.getName());
        model.addAttribute("hostAddress", hostInfo.getAddress());

        //????????????
        int articleTypeCount = articleTypeService.count();
        int articleTagCount = articleTagListService.count();
        int articleCount = articleService.count();
        model.addAttribute("articleTypeCount", articleTypeCount);
        model.addAttribute("articleTagCount", articleTagCount);
        model.addAttribute("articleCount", articleCount);

        //????????????
        int userCount = userService.count();
        model.addAttribute("userCount", userCount);

        return "/admin/adminIndex";
    }


    /**
     * ????????? - ????????????
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
     * ????????????
     *
     * @param userId
     * @return
     */
    @PostMapping("/user/del")
    @ResponseBody
    public CommonResult userDel(String userId) {
        if (StrUtil.isBlank(userId)) {
            return CommonResult.failed("????????????????????????????????????");
        }
        if (articleService.count(Wrappers.<Article>lambdaQuery().eq(Article::getUserId, userId)) > 0) {
            return CommonResult.failed("?????????????????????????????????????????????????????????");
        }

        if (userService.removeById(userId)) {
            return CommonResult.success("????????????");
        }

        return CommonResult.failed("????????????");
    }


    /**
     * ????????????
     *
     * @param userDto
     * @return
     */
    @PostMapping("/user/update")
    @ResponseBody
    public CommonResult userUpdate(@Valid UserDto userDto) {
        User user = userService.getById(userDto.getUserId());
        if (Objects.isNull(user)) {
            return CommonResult.failed("??????id ?????????");
        }
        Date userRegisterTime = user.getUserRegisterTime();
        String userPassword = userDto.getUserPassword();
        if (StrUtil.isNotBlank(userPassword)) {
            //???????????? = md5??????????????? + ??????????????????)
            userDto.setUserPassword(SecureUtil.md5(userRegisterTime + userPassword));
        } else {
            userDto.setUserPassword(null);
        }
        BeanUtils.copyProperties(userDto, user);


        if (userService.updateById(user)) {
            return CommonResult.success("????????????");
        }

        return CommonResult.failed("????????????,?????????");
    }

    /**
     * ???????????????????????????????????????
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
     * ??????????????????
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
            return CommonResult.failed("??????????????????????????????????????????");
        }

        if (StrUtil.isBlank(articleTypeId)) {
            articleType.setArticleTypeAddTime(DateUtil.date());
            if (articleTypeService.save(articleType)) {

                return CommonResult.success("????????????");
            }
        }
        if (articleTypeService.updateById(articleType)) {
            return CommonResult.success("????????????");
        }

        return CommonResult.failed("????????????");
    }


    /**
     * ??????????????????
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
            return CommonResult.failed("??????????????????????????????????????????");
        }

        if (articleTypeService.updateById(articleType)) {
            servletContext.removeAttribute("articleTypeList");
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }


    /**
     * ??????????????????
     *
     * @param articleTypeId
     * @return
     */
    @PostMapping("/article/type/del")
    @ResponseBody
    public CommonResult articleTypeDel(@NotBlank(message = "????????????id ????????????") String articleTypeId) {
        if (articleService.count(Wrappers.<Article>lambdaQuery()
                .eq(Article::getArticleTypeId, articleTypeId)) > 0) {
            return CommonResult.failed("?????????????????????????????????");
        }

        if (articleTypeService.count(Wrappers.<ArticleType>lambdaQuery().eq(ArticleType::getArticleTypeParentId, articleTypeId)) > 0) {
            return CommonResult.failed("????????????????????????");
        }

        if (articleTypeService.removeById(articleTypeId)) {
            servletContext.removeAttribute("articleTypeList");
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }


    /**
     * ???????????????
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
     * ???????????? ??????
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
                return CommonResult.success("????????????");
            }
            return CommonResult.failed("????????????");
        }


        articleTag.setArticleTagAddTime(DateUtil.date());
        if (articleTagService.save(articleTag)) {
            return CommonResult.success("????????????????????????");
        }
        return CommonResult.failed("????????????????????????");
    }

    /**
     * ???????????? ??????
     *
     * @param articleTagId
     * @return
     */
    @PostMapping("/article/tag/del")
    @ResponseBody
    public CommonResult articleTagDel(String articleTagId) {
        if (StrUtil.isBlank(articleTagId)) {
            return CommonResult.failed("??????????????????????????????????????????id");
        }

        if (articleTagListService.count(Wrappers.<ArticleTagList>lambdaQuery()
                .eq(ArticleTagList::getArticleTagId, articleTagId)) > 0) {
            return CommonResult.failed("???????????????????????????????????????????????????????????????????????????");
        }

        if (articleTagService.removeById(articleTagId)) {
            servletContext.removeAttribute("articleTagList");
            return CommonResult.success("??????????????????");
        }
        return CommonResult.failed("??????????????????");
    }

    /**
     * ????????????
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
     * ?????????????????????
     *
     * @param articleId
     * @return
     */
    @PostMapping("/article/hot")
    @ResponseBody
    public CommonResult articleHot(String articleId) {
        if (articleService.update(Wrappers.<Article>lambdaUpdate().eq(Article::getArticleId,articleId).set(Article::getArticleHot,1))) {
            servletContext.removeAttribute("articleHotList");
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }

    /**
     * ????????????
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
     * ????????????????????????
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
     * ????????????
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
            //????????????
            link.setLinkAddTime(DateUtil.date());
            if (linkService.save(link)) {
                return CommonResult.success("????????????");
            }
            return CommonResult.failed("????????????");
        }

        if (linkService.updateById(link)) {
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");

    }

    /**
     * ????????????
     *
     * @param linkId
     * @return
     */
    @PostMapping("/link/del")
    @ResponseBody
    public CommonResult linkDel(String linkId) {
        if (linkService.removeById(linkId)) {
            servletContext.removeAttribute("linkList");
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }

    /**
     * ????????????
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
     * ??????????????????
     *
     * @param adType
     * @return
     */
    @PostMapping("/ad/type/addOrUpdate")
    @ResponseBody
    public CommonResult adTypeAddOrUpdate(AdType adType) {
        String adTypeId = adType.getAdTypeId();
        if (StrUtil.isBlank(adTypeId)) {
            //??????????????????
            adType.setAdTypeAddTime(DateUtil.date());
            if (adTypeService.save(adType)) {
                return CommonResult.success("????????????");
            }
            return CommonResult.success("????????????");
        }

        //??????????????????
        if (adTypeService.updateById(adType)) {
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }


    /**
     * ????????????
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

        //????????????????????????
        servletContext.removeAttribute("adIndexList");
        servletContext.removeAttribute("adArticleList");

        if (StrUtil.isBlank(adId)) {
            //??????????????????
            ad.setAdAddTime(DateUtil.date());
            if (adService.save(ad)) {
                return CommonResult.success("????????????");
            }
            return CommonResult.success("????????????");
        }

        //??????????????????
        if (adService.updateById(ad)) {
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }

    /**
     * ????????????
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
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }

    /**
     * ??????admin??????
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
                    return CommonResult.success("????????????");
                }
            }
        }
        return CommonResult.failed("????????????");
    }


}
