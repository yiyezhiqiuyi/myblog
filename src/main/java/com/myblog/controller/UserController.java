package com.myblog.controller;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.myblog.dto.article.PublishArticleActionDto;
import com.myblog.entity.*;
import com.myblog.service.*;
import com.myblog.utils.CommonPage;
import com.myblog.utils.CommonResult;
import com.myblog.vo.ArticleVo;
import com.myblog.vo.CommentVo;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserCollectionArticleService userCollectionArticleService;
    @Autowired
    private IArticleService articleService;
    @Autowired
    private IArticleTypeService articleTypeService;
    @Autowired
    private IArticleTagService articleTagService;
    @Autowired
    private IArticleTagListService articleTagListService;
    @Autowired
    private IUploadFileListService uploadFileListService;
    @Autowired
    private ICommentService commentService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ICommentReplyService commentReplyService;

    /**
     * ????????????
     *
     * @param file
     * @return
     */
    @PostMapping("/uploadFile")
    @ResponseBody
    public String uploadFile(HttpServletRequest request, MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }
        User user = (User) request.getSession().getAttribute("user");
        if (Objects.isNull(user.getUserPublishArticle()) || user.getUserPublishArticle() != 1) {
            return null;
        }

        return uploadFileListService.getUploadFileUrl(file);
    }


    /**
     * ??????????????????
     *
     * @return
     */
    @GetMapping("/manager")
    public String userManager() {
        return "/user/userManager";
    }


    /**
     * ????????????
     *
     * @param pageNumber
     * @return
     */
    @GetMapping("/collection/list")
    public String collectionList(HttpServletRequest request, Integer pageNumber, Model model) {
        User user = (User) request.getSession().getAttribute("user");

        Page<Article> articlePage = new Page<>(pageNumber, 24);


        List<String> articleIdList = userCollectionArticleService.list(Wrappers.<UserCollectionArticle>lambdaQuery()
                        .eq(UserCollectionArticle::getUserId, user.getUserId())
                        .select(UserCollectionArticle::getArticleId)).stream()
                .map(UserCollectionArticle::getArticleId)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(articleIdList)) {
            LambdaQueryWrapper<Article> wrapper = Wrappers.<Article>lambdaQuery()
                    .in(Article::getArticleId, articleIdList)
                    .select(Article::getArticleId,
                            Article::getArticleAddTime,
                            Article::getArticleCollectionNumber,
                            Article::getArticleGoodNumber,
                            Article::getArticleLookNumber,
                            Article::getArticleCoverUrl,
                            Article::getArticleTitle);
            IPage<Article> articleIPage = articleService.page(articlePage, wrapper);
            model.addAttribute("articleIPage", CommonPage.restPage(articleIPage));
        }


        return "/user/collectionList";
    }

    /**
     * ????????????
     *
     * @param pageNumber
     * @return
     */
    @GetMapping("/article/list")
    public String articleList(HttpServletRequest request, Integer pageNumber, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        Page<ArticleVo> articlePage = new Page<>(pageNumber, 24);
        IPage<ArticleVo> articleVoIPage = articleService.articleList(articlePage, null, null);
        model.addAttribute("articleVoIPage", CommonPage.restPage(articleVoIPage));
        return "/user/articleList";
    }


    /**
     * ????????????
     *
     * @return
     */
    @GetMapping("/publishArticle")
    public String releaseArticle(HttpServletRequest request, Model model, String articleId) {
        User user = (User) request.getSession().getAttribute("user");
        if (Objects.isNull(user) || Objects.isNull(user.getUserPublishArticle()) || user.getUserPublishArticle() != 1) {
            return "redirect:/";
        }

        Article article = articleService.getOne(Wrappers.<Article>lambdaQuery().eq(Article::getUserId, user.getUserId()).eq(Article::getArticleId, articleId));
        if (Objects.nonNull(article)) {
            model.addAttribute("article", article);

            //??????????????????
            List<ArticleTagList> articleTagLists = articleTagListService.list(Wrappers.<ArticleTagList>lambdaQuery()
                    .eq(ArticleTagList::getArticleId, article.getArticleId())
                    .select(ArticleTagList::getArticleTagId));

            if (CollUtil.isNotEmpty(articleTagLists)) {
                List<String> articleTagIdList = articleTagLists.stream().map(ArticleTagList::getArticleTagId).collect(Collectors.toList());
                model.addAttribute("articleTagIdList", articleTagIdList);
            }
            //????????????????????????????????????
            String articleTypeId = article.getArticleTypeId();
            List<ArticleType> articleSameTypeList = articleTypeService.list(Wrappers.<ArticleType>lambdaQuery().eq(ArticleType::getArticleTypeParentId, articleTypeService.getById(articleTypeId).getArticleTypeParentId()));
            model.addAttribute("articleSameTypeList", articleSameTypeList);

            //???????????????????????????
            model.addAttribute("articleTypeParentId", articleTypeService.getById(article.getArticleTypeId()).getArticleTypeParentId());
        }

        //????????????
        List<ArticleType> articleType0List = articleTypeService.list(Wrappers.<ArticleType>lambdaQuery().isNull(ArticleType::getArticleTypeParentId).or().eq(ArticleType::getArticleTypeParentId, "").orderByAsc(ArticleType::getArticleTypeSort));
        model.addAttribute("articleType0List", articleType0List);

        //????????????
        List<ArticleTag> articleTagList = articleTagService.list();
        model.addAttribute("articleTagList", articleTagList);

        return "/user/publishArticle";
    }

    /**
     * ????????????????????????
     *
     * @param articleTypeId
     * @return
     */
    @PostMapping("/getArticleTypeChild")
    @ResponseBody
    public CommonResult getArticleTypeChild(String articleTypeId) {
        if (StrUtil.isBlank(articleTypeId)) {
            return CommonResult.failed("?????????????????????");
        }

        List<ArticleType> articleTypeList = articleTypeService.list(Wrappers.<ArticleType>lambdaQuery()
                .eq(ArticleType::getArticleTypeParentId, articleTypeId)
                .select(ArticleType::getArticleTypeId, ArticleType::getArticleTypeName));

        return CommonResult.success(articleTypeList);
    }


    /**
     * ??????????????????
     *
     * @param publishArticleActionDto
     * @return
     */
    @PostMapping("/publishArticleAction")
    @ResponseBody
    public CommonResult publishArticleAction(HttpServletRequest request, @Valid PublishArticleActionDto publishArticleActionDto, MultipartFile articleCoverFile) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (Objects.isNull(user)){
            return CommonResult.failed("??????????????????????????????");
        }
        User serviceById = userService.getById(user.getUserId());
        if(Objects.isNull(serviceById)){
            session.setAttribute("user",serviceById);
        }
        if (Objects.isNull(serviceById.getUserPublishArticle()) || serviceById.getUserPublishArticle() != 1) {
            return CommonResult.failed("???????????????????????????????????????????????????");
        }
        if (Objects.nonNull(articleCoverFile)) {
            //????????????????????????????????????????????????????????????
            BufferedImage read = ImageIO.read(articleCoverFile.getInputStream());
            if (Objects.isNull(read)) {
                return CommonResult.failed("?????????????????????");
            }
            int width = read.getWidth();
            int height = read.getHeight();
            if (width != 300 || height != 150) {
                return CommonResult.failed("?????????????????? 300px * 150px");
            }


            publishArticleActionDto.setArticleCoverUrl(uploadFileListService.getUploadFileUrl(articleCoverFile));
        }
        return articleService.publishArticleAction(request, publishArticleActionDto);
    }

    /**
     * ???????????????????????????
     *
     * @param pageNumber
     * @param model
     * @return
     */
    @GetMapping("/myArticleList")
    public String myArticleList(HttpServletRequest request, Integer pageNumber, Model model) {
        User user = (User) request.getSession().getAttribute("user");
        if (Objects.isNull(pageNumber) || pageNumber < 1) {
            pageNumber = 1;
        }
        Page<ArticleVo> articlePage = new Page<>(pageNumber, 24);
        IPage<ArticleVo> articleVoIPage = articleService.articleList(articlePage, null, user.getUserId());
        model.addAttribute("articleVoIPage", CommonPage.restPage(articleVoIPage));

        return "/user/myArticleList";
    }

    /**
     * ????????????
     *
     * @param articleId
     * @return
     */
    @PostMapping("/delArticle")
    @ResponseBody
    public CommonResult delArticle(String articleId) {
        return articleService.delArticle(articleId);
    }


    /**
     * ????????????
     *
     * @param request
     * @param articleId
     * @param commentContent
     * @return
     */
    @PostMapping("/saveComment")
    @ResponseBody
    public CommonResult userSaveComment(HttpServletRequest request, String articleId, String commentContent,String commentId) {
        if (StrUtil.isBlank(articleId) || StrUtil.isBlank(commentContent)) {
            return CommonResult.failed("????????????????????????????????????");
        }
        if (commentContent.length() < 1 || commentContent.length() > 800) {
            return CommonResult.failed("???????????????1-800??????????????????");
        }


        User user = (User) request.getSession().getAttribute("user");
        if (Objects.isNull(user)) {
            return CommonResult.failed("???????????????????????????????????????");
        }
        String userId = user.getUserId();


        Comment comment = commentService.getOne(Wrappers.<Comment>lambdaQuery().eq(Comment::getUserId, userId).select(Comment::getCommentTime).orderByDesc(Comment::getCommentTime), false);
        if (Objects.nonNull(comment) && Objects.nonNull(comment.getCommentTime())) {
            if ((comment.getCommentTime().getTime() + 10000) > System.currentTimeMillis()) {
                return CommonResult.failed("??????????????????~~??????????????????");
            }
        }

        Comment comment1 = new Comment();
        comment1.setArticleId(articleId);
        comment1.setUserId(userId);
        comment1.setCommentContent(commentContent);
        comment1.setCommentTime(DateUtil.date());
        comment1.setCommentGoodNumber(0);

        if (commentService.save(comment1)) {
            CommentVo commentVo = new CommentVo();
            BeanUtils.copyProperties(comment1, commentVo);
            commentVo.setUserName(
                    userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUserId, commentVo.getUserId()).select(User::getUserName)).getUserName()
            );
            commentVo.setCommentTime(DateUtil.format(comment1.getCommentTime(),"yyyy-MM-dd HH:mm:ss"));
            return CommonResult.success(commentVo);
        }
        return CommonResult.failed("????????????");
    }


}
