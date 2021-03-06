package com.myblog.controller;

import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.dto.user.UserInfoDto;
import com.myblog.entity.*;
import com.myblog.service.*;
import com.myblog.utils.CommonPage;
import com.myblog.utils.CommonResult;
import com.myblog.utils.CommonUtils;
import com.myblog.vo.ArticleVo;
import com.myblog.vo.CommentVo;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    @Autowired
    private IUserService userService;
    @Autowired
    private IArticleService articleService;
    @Autowired
    private IArticleTypeService articleTypeService;
    @Autowired
    private IArticleTagService articleTagService;
    @Autowired
    private IArticleTagListService articleTagListService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private ICommentReplyService commentReplyService;
    @Autowired
    private ServletContext servletContext;

    /**
     * ??????????????????
     *
     * @return
     */
    @GetMapping("/ci")
    public String clearCache() {
        servletContext.removeAttribute("articleTypeList");
        servletContext.removeAttribute("articleHotList");
        servletContext.removeAttribute("articleTagList");
        servletContext.removeAttribute("adIndexList");
        servletContext.removeAttribute("linkList");
        return "redirect:/";
    }

    /**
     * ?????????????????????
     *
     * @throws IOException
     */
    @GetMapping("/getCaptcha")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        CircleCaptcha captcha = CommonUtils.getCaptcha(request);
        captcha.write(response.getOutputStream());
    }

    /**
     * ??????????????????
     *
     * @param request
     * @return
     */
    @GetMapping("/register")
    public String register(HttpServletRequest request) {
        if (Objects.nonNull(request.getSession().getAttribute("user"))) {
            return "redirect:/";
        }

        return "/view/register";
    }

    /**
     * ??????????????????
     *
     * @param request
     * @param userInfoDto
     * @return
     */
    @PostMapping("/userRegister")
    @ResponseBody
    public CommonResult userRegister(HttpServletRequest request, UserInfoDto userInfoDto) {
        HttpSession session = request.getSession();
        String verifyCode = userInfoDto.getVerifyCode();
        if (StrUtil.isBlank(verifyCode) || !verifyCode.equals(session.getAttribute("circleCaptchaCode"))) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("??????????????????");
        }
        //??????????????????????????????
        if (userInfoDto.getUserName().equals(userInfoDto.getUserPassword())) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("?????????????????????????????????");
        }

        //??????????????????????????????
        if (userService.count(Wrappers.<User>lambdaQuery().eq(User::getUserName, userInfoDto.getUserName())) > 0) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("???????????????????????????");
        }

        User user = new User();
        BeanUtils.copyProperties(userInfoDto, user);
        user.setUserId(IdUtil.simpleUUID());
        user.setUserRegisterTime(DateUtil.date());
        user.setUserPassword(SecureUtil.md5(user.getUserId() + user.getUserPassword()));
        user.setUserFrozen(0);
        user.setUserPublishArticle(0);
        if (userService.save(user)) {
            return CommonResult.success("????????????");
        }

        return CommonResult.failed("??????????????????????????????????????????????????????~");
    }


    /**
     * ??????????????????
     *
     * @param request
     * @return
     */
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        if (Objects.nonNull(request.getSession().getAttribute("user"))) {
            return "redirect:/";
        }
        model.addAttribute("referer", request.getHeader("referer"));
        return "/view/login";
    }


    /**
     * ??????????????????
     *
     * @param request
     * @param userInfoDto
     * @return
     */
    @PostMapping("/userLogin")
    @ResponseBody
    public CommonResult userLogin(HttpServletRequest request, UserInfoDto userInfoDto) {
        HttpSession session = request.getSession();
        String verifyCode = userInfoDto.getVerifyCode();
        if (StrUtil.isBlank(verifyCode) || !verifyCode.equals(session.getAttribute("circleCaptchaCode"))) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("??????????????????");
        }
        //??????????????????????????????
        if (userInfoDto.getUserName().equals(userInfoDto.getUserPassword())) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("?????????????????????????????????");
        }

        //????????????
        User userDb = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUserName, userInfoDto.getUserName()), false);
        if (Objects.isNull(userDb)) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("???????????????");
        }
        if (Objects.nonNull(userDb.getUserFrozen()) && userDb.getUserFrozen() == 1) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("??????????????????????????????????????????????????????????????????");
        }

        if (!SecureUtil.md5(userDb.getUserId() + userInfoDto.getUserPassword()).equals(userDb.getUserPassword())) {
            session.removeAttribute("circleCaptchaCode");
            return CommonResult.failed("????????????");
        }
        session.setAttribute("user", userDb);
        return CommonResult.success("????????????");
    }

    /**
     * ??????????????????
     *
     * @param request
     * @return
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return "/";
    }

    /**
     * ??????
     *
     * @return
     */
    @GetMapping("/")
    public String index(HttpServletRequest request, Model model) {


        return "/view/index";
    }

    /**
     * ????????????
     *
     * @param pageNumber
     * @return
     */
    @GetMapping("/article/list")
    public String articleListView(Integer pageNumber, String articleTitle, String articleTypeId, Model model) {
        Page<ArticleVo> articlePage = new Page<>(Objects.isNull(pageNumber) ? 1 : pageNumber, 24);
        IPage<ArticleVo> articleVoIPage = articleService.articleListView(articlePage, articleTitle, articleTypeId);

        //????????????
        model.addAttribute("articleVoIPage", CommonPage.restPage(articleVoIPage));

        //??????????????????
        if (StrUtil.isNotBlank(articleTypeId)) {
            ArticleType articleType = articleTypeService.getOne(Wrappers.<ArticleType>lambdaQuery().eq(ArticleType::getArticleTypeId, articleTypeId).select(ArticleType::getArticleTypeName), false);
            model.addAttribute("articleTypeName", articleType.getArticleTypeName());
            model.addAttribute("articleTypeId", articleTypeId);
        }

        return "/view/articleList";
    }


    /**
     * ?????????????????????????????????
     *
     * @param articleTagId
     * @param pageNumber
     * @return
     */
    @GetMapping("/tag/article/list")
    public String tagArticleList(String articleTagId, Integer pageNumber, Model model) {
        if (StrUtil.isBlank(articleTagId)) {
            return "redirect:/";
        }
        if (Objects.isNull(pageNumber) || pageNumber < 1) {
            pageNumber = 1;
        }
        Page<ArticleVo> articlePage = new Page<>(Objects.isNull(pageNumber) ? 1 : pageNumber, 24);
        IPage<ArticleVo> articleVoIPage = articleService.tagArticleList(articlePage, articleTagId);
        model.addAttribute("articleVoIPage", CommonPage.restPage(articleVoIPage));

        //??????????????????
        ArticleTag articleTag = articleTagService.getOne(Wrappers.<ArticleTag>lambdaQuery().eq(ArticleTag::getArticleTagId, articleTagId));
        if (Objects.nonNull(articleTag)) {
            model.addAttribute("articleTagName", articleTag.getArticleTagName());
        }

        model.addAttribute("articleTagId", articleTagId);
        return "/view/tagArticleList";

    }

    /**
     * ??????
     *
     * @param articleId
     * @return
     */
    @GetMapping("/article")
    public String articleView(HttpServletRequest request, String articleId, Model model) {
        HttpSession session = request.getSession();

        ArticleVo articleVo = articleService.getArticle(articleId);
        if (Objects.isNull(articleVo)) {
            return "redirect:/";
        }

        Article article = articleService.getOne(Wrappers.<Article>lambdaQuery().eq(Article::getArticleId, articleVo.getArticleId()).select(Article::getArticleId, Article::getArticleLookNumber), false);
        //??????????????????
        Integer articleLookNumber = article.getArticleLookNumber();
        if (Objects.isNull(articleLookNumber) || articleLookNumber < 0) {
            articleLookNumber = 0;
        }
        ++articleLookNumber;
        article.setArticleLookNumber(articleLookNumber);
        articleService.updateById(article);


        //?????????????????????
        String userName = articleVo.getUserName();
        if (StrUtil.isNotBlank(userName)) {
            articleVo.setUserName(CommonUtils.getHideMiddleStr(userName));
        }

        //??????
        model.addAttribute("article", articleVo);

        //????????????
        if (Objects.nonNull(articleVo) && StrUtil.isNotBlank(articleVo.getArticleTypeId())) {
            ArticleType articleType = articleTypeService.getOne(Wrappers.<ArticleType>lambdaQuery().eq(ArticleType::getArticleTypeId, articleVo.getArticleTypeId()).select(ArticleType::getArticleTypeName, ArticleType::getArticleTypeId), false);
            model.addAttribute("articleType", articleType);
        }

        return "/view/article";
    }

    /**
     * ????????????????????????
     *
     * @param articleId
     * @param pageNumber
     * @return
     */
    @PostMapping("/comment/list")
    @ResponseBody
    public CommonResult commentList(HttpServletRequest request, String articleId, Integer pageNumber) {
        if (StrUtil.isBlank(articleId)) {
            return CommonResult.failed("??????????????????????????????????????????");
        }
        if (Objects.isNull(pageNumber) || pageNumber < 1) {
            pageNumber = 1;
        }
        Page<CommentVo> commentVoPage = new Page<>(pageNumber, 5);
        IPage<CommentVo> commentVoIPage = commentService.getArticleCommentList(commentVoPage, articleId);
        commentVoIPage.getRecords().stream().forEach(commentVo -> {
            commentVo.setUserName(CommonUtils.getHideMiddleStr(commentVo.getUserName()));
        });

        //????????????????????????
        HashMap<String, Long> goodCommentMap = (HashMap<String, Long>) request.getSession().getAttribute("goodCommentMap");
        if (CollUtil.isNotEmpty(goodCommentMap)) {
            List<String> commentIds = goodCommentMap.keySet().stream().collect(Collectors.toList());
            commentVoIPage.getRecords().stream().forEach(commentVo -> {
                if (commentIds.contains(commentVo.getCommentId())) {
                    commentVo.setIsGoodComment(1);
                }
            });
        }

        return CommonResult.success(CommonPage.restPage(commentVoIPage));
    }


    /**
     * ??????
     *
     * @return
     */
    @GetMapping("/contact")
    public String contact() {
        return "/view/contact";
    }


    /**
     * ????????????
     *
     * @param request
     * @param articleId
     * @return
     */
    @PostMapping("/articleGood")
    @ResponseBody
    public CommonResult articleGood(HttpServletRequest request, String articleId) {
        HttpSession session = request.getSession();
        if (Objects.nonNull(session.getAttribute("articleGoodTime"))) {
            return CommonResult.failed("??????????????????");
        }

        Article article = articleService.getById(articleId);
        Integer articleGoodNumber = article.getArticleGoodNumber();
        ++articleGoodNumber;
        article.setArticleGoodNumber(articleGoodNumber);
        if (articleService.updateById(article)) {
            session.setAttribute("articleGoodTime", true);
            return CommonResult.success("???????????????");
        }

        return CommonResult.failed("????????????");
    }


    /**
     * ????????????
     *
     * @param request
     * @param articleId
     * @return
     */
    @PostMapping("/articleCollection")
    @ResponseBody
    public CommonResult articleCollection(HttpServletRequest request, String articleId) {
        User user = (User) request.getSession().getAttribute("user");
        if (Objects.isNull(user)) {
            return CommonResult.failed("?????????????????????");
        }
        return articleService.articleCollection(user, articleId);
    }

    /**
     * ????????????
     *
     * @param request
     * @param articleTitle
     * @return
     */
    @GetMapping("/article/search")
    public String articleSearch(HttpServletRequest request, Integer pageNumber, String articleTitle, Model model) {
        if (StrUtil.isBlank(articleTitle)) {
            return "/";
        }
        articleTitle = articleTitle.trim();
        model.addAttribute("articleTitle", articleTitle);
        if (Objects.isNull(pageNumber) || pageNumber < 1) {
            pageNumber = 1;
        }
        String ipAddr = CommonUtils.getIpAddr(request);
        ServletContext servletContext = request.getServletContext();
        ConcurrentMap<String, Long> articleSearchMap = (ConcurrentMap<String, Long>) servletContext.getAttribute("articleSearchMap");
        if (CollUtil.isEmpty(articleSearchMap) || Objects.isNull(articleSearchMap.get(ipAddr))) {
            articleSearchMap = new ConcurrentHashMap<>();
            articleSearchMap.put(ipAddr, DateUtil.currentSeconds());
        } else {
            if ((articleSearchMap.get(ipAddr) + 1 > DateUtil.currentSeconds())) {
                return "/view/searchError";
            }
        }
        //????????????????????????
        List<Article> articleList = new ArrayList<>();

        //???????????????,????????????
        List<Word> words = WordSegmenter.seg(articleTitle);
        List<String> titleList = words.stream().map(Word::getText).collect(Collectors.toList());
        titleList.add(articleTitle);
        List<String> articleTagIdList = articleTagService.list(Wrappers.<ArticleTag>lambdaQuery()
                .in(ArticleTag::getArticleTagName, titleList)
                .select(ArticleTag::getArticleTagId)).stream().map(ArticleTag::getArticleTagId).collect(Collectors.toList());
        List<String> articleIdList = new ArrayList<>();
        if (CollUtil.isNotEmpty(articleTagIdList)) {
            articleIdList = articleTagListService.list(Wrappers.<ArticleTagList>lambdaQuery()
                    .in(ArticleTagList::getArticleTagId, articleTagIdList)
                    .select(ArticleTagList::getArticleId)).stream()
                    .map(ArticleTagList::getArticleId).collect(Collectors.toList());

        }

        //????????????
        IPage<Article> articlePage = new Page<>(pageNumber, 12);
        LambdaQueryWrapper<Article> queryWrapper = Wrappers.<Article>lambdaQuery()
                .like(Article::getArticleTitle, articleTitle)
                .select(Article::getArticleId,
                        Article::getArticleCoverUrl,
                        Article::getArticleCollectionNumber,
                        Article::getArticleLookNumber,
                        Article::getArticleAddTime,
                        Article::getArticleTitle);
        if (CollUtil.isNotEmpty(articleIdList)) {
            queryWrapper.or().in(Article::getArticleId, articleIdList);
        }

        IPage<Article> articleIPage = articleService.page(articlePage, queryWrapper);
        model.addAttribute("articleIPage", CommonPage.restPage(articleIPage));

        //??????????????????
        articleSearchMap.put(ipAddr, DateUtil.currentSeconds());
        servletContext.setAttribute("articleSearchMap", articleSearchMap);

        return "/view/articleSearch";
    }

    /**
     * ???????????????
     *
     * @param commentId
     * @return
     */
    @PostMapping("/goodComment")
    @ResponseBody
    public CommonResult goodComment(HttpServletRequest request, String commentId) {
        HttpSession session = request.getSession();


        if (StrUtil.isBlank(commentId)) {
            return CommonResult.failed("???????????????????????????????????????????????????");
        }

        //???????????????????????????????????????
        HashMap<String, Long> goodCommentMap = (HashMap<String, Long>) session.getAttribute("goodCommentMap");
        if (CollUtil.isEmpty(goodCommentMap)) {
            goodCommentMap = new HashMap<>();
        } else {
            if (Objects.nonNull(goodCommentMap.get(commentId))) {
                Long goodCommentTime = goodCommentMap.get(commentId);
                if ((goodCommentTime + 3600) >= DateUtil.currentSeconds()) {
                    return CommonResult.failed("????????????????????????????????????");
                }
            }
        }

        Comment comment = commentService.getById(commentId);
        if (Objects.isNull(comment)) {
            return CommonResult.failed("??????????????????????????????????????????????????????????????????");
        }
        Integer commentGoodNumber = comment.getCommentGoodNumber();
        ++commentGoodNumber;
        if (commentService.updateById(comment.setCommentGoodNumber(commentGoodNumber))) {
            goodCommentMap.put(commentId, DateUtil.currentSeconds());
            session.setAttribute("goodCommentMap", goodCommentMap);
            return CommonResult.success("????????????");
        }
        return CommonResult.failed("????????????");
    }

}
