package com.iblog.controller.admin;

import com.iblog.bean.Article;
import com.iblog.bean.RespBean;
import com.iblog.bean.User;
import com.iblog.service.ArticleService;
import com.iblog.utils.Util;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author maple
 * @Description
 * @createTime:2025-11-23 20:26
 */
@RestController
@RequestMapping("/article")
public class ArticleController {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    @PostMapping(value = "/")
    public RespBean addNewArticle(Article article) {
        log.debug("调用 addNewArticle, article={}", Util.toJson(article));
        // 创建文章需要已登录（security 已做一层校验），但更新操作还需校验是否为作者或管理员
        User current = Util.getCurrentUser();
        if (article.getId() == null || article.getId() == -1) {
            // 新建文章：必须登录
            if (current == null || current.getId() == null) {
                return new RespBean("error", "未登录，无法创建文章");
            }
            int result = articleService.addNewArticle(article);
            if (result == 1) {
                log.info("文章保存/发布成功, id={}", article.getId());
                return new RespBean("success", article.getId() + "");
            } else {
                log.warn("文章保存/发布失败, result={}, article={}", result, Util.toJson(article));
                return new RespBean("error", article.getState() == 0 ? "文章保存失败!" : "文章发表失败!");
            }
        } else {
            // 更新：必须为文章作者或管理员
            Long ownerId = articleService.getArticleOwnerId(article.getId());
            if (!isAuthorOrAdmin(ownerId)) {
                return new RespBean("error", "没有权限编辑此文章");
            }
            int result = articleService.addNewArticle(article);
            if (result == 1) {
                log.info("文章更新成功, id={}", article.getId());
                return new RespBean("success", article.getId() + "");
            } else {
                log.warn("文章更新失败, result={}, article={}", result, Util.toJson(article));
                return new RespBean("error", "文章更新失败!");
            }
        }
    }

    /**
     * 上传图片
     * @return 返回值为图片的地址
     */
    @PostMapping(value = "/uploadimg")
    public RespBean uploadImg(HttpServletRequest req, MultipartFile image) {
        log.debug("调用 uploadImg, originalFilename={}", image == null ? "null" : image.getOriginalFilename());
        StringBuilder url = new StringBuilder();
        String filePath = "/blogimg/" + DATE_FMT.format(LocalDate.now());
        String imgFolderPath = req.getServletContext().getRealPath(filePath);
        File imgFolder = new File(imgFolderPath);
        if (!imgFolder.exists()) {
            boolean isCreated = imgFolder.mkdirs();
            if (!isCreated) {
                log.error("创建图片上传目录失败: {}", imgFolderPath);
                return new RespBean("error", "上传失败!");
            }
        }
        url.append(req.getScheme())
                .append("://")
                .append(req.getServerName())
                .append(":")
                .append(req.getServerPort())
                .append(req.getContextPath())
                .append(filePath);
        String imgName = UUID.randomUUID() + "_" + (image == null ? "" : Objects.requireNonNull(image.getOriginalFilename()).replaceAll(" ", ""));
        try {
            IOUtils.write(image != null ? image.getBytes() : null, Files.newOutputStream(new File(imgFolder, imgName).toPath()));
            url.append("/").append(imgName);
            log.info("图片上传成功: {}", url);
            return new RespBean("success", url.toString());
        } catch (IOException e) {
            log.error("图片上传失败: {}", e.getMessage(), e);
        }
        return new RespBean("error", "上传失败!");
    }
    @GetMapping(value = "/all")
    public Map<String, Object> getArticleByState(@RequestParam(value = "state", defaultValue = "-1") Integer state,
                                                 @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                 @RequestParam(value = "count", defaultValue = "6") Integer count,
                                                 String keywords) {
        // 公共文章列表（不按用户过滤）
        Long uid = null; // null 表示不按用户过滤
        int totalCount = articleService.getArticleCountByStateForUid(state, uid, keywords);
        List<Article> articles = articleService.getArticleByStateForUid(state, page, count, keywords, uid);
        Map<String, Object> map = new HashMap<>();
        map.put("totalCount", totalCount);
        map.put("articles", articles);
        return map;
    }

    @GetMapping(value = "/{aid}")
    public Article getArticleById(@PathVariable Long aid) {
        log.debug("调用 getArticleById, aid={}", aid);
        return articleService.getArticleById(aid);
    }

    @PutMapping(value = "/dustbin")
    public RespBean updateArticleState(@RequestBody List<Long> aids, Integer state) {
        log.debug("调用 updateArticleState, aids={}, state={}", Util.toJson(aids), state);
        User current = Util.getCurrentUser();
        if (current == null || current.getId() == null) {
            return new RespBean("error", "未登录，无法操作");
        }
        // 检查每个文章是否可由当前用户操作（作者或管理员）
        for (Long aid : aids) {
            Long ownerId = articleService.getArticleOwnerId(aid);
            if (!isAuthorOrAdmin(ownerId)) {
                return new RespBean("error", "没有权限操作文章 id=" + aid);
            }
        }
        if (articleService.updateArticleState(aids, state) == aids.size()) {
            log.info("文章状态更新成功, aids={}", Util.toJson(aids));
            return new RespBean("success", "操作成功");
        }
        log.warn("文章状态更新失败, aids={}", Util.toJson(aids));
        return new RespBean("error", "操作失败!");
    }

    /**
     * 当前用户的文章（如果未登录则返回空集合和 totalCount=0）
     */
    @GetMapping(value = "/my")
    public Map<String, Object> getMyArticles(@RequestParam(value = "state", defaultValue = "-1") Integer state,
                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "count", defaultValue = "6") Integer count,
                                             String keywords) {
        User u = Util.getCurrentUser();
        Map<String, Object> map = new HashMap<>();
        if (u == null || u.getId() == null) {
            // 未登录：返回空结果（前端可以据此提示登录）
            map.put("totalCount", 0);
            map.put("articles", java.util.Collections.emptyList());
            return map;
        }
        Long uid = u.getId();
        int totalCount = articleService.getArticleCountByStateForUid(state, uid, keywords);
        List<Article> articles = articleService.getArticleByStateForUid(state, page, count, keywords, uid);
        map.put("totalCount", totalCount);
        map.put("articles", articles);
        return map;
    }

    /**
     * 判断当前用户是否为文章作者或具有超级管理员角色
     */
    private boolean isAuthorOrAdmin(Long ownerId) {
        User u = Util.getCurrentUser();
        if (u == null || u.getId() == null) return false;
        if (ownerId != null && ownerId.equals(u.getId())) return true;
        if (u.getRoles() != null) {
            for (Role r : u.getRoles()) {
                if ("超级管理员".equals(r.getName())) return true;
            }
        }
        return false;
    }

    @RequestMapping("/dataStatistics")
    public Map<String,Object> dataStatistics() {
        log.debug("调用 dataStatistics");
        Map<String, Object> map = new HashMap<>();
        List<String> categories = articleService.getCategories();
        List<Integer> dataStatistics = articleService.getDataStatistics();
        map.put("categories", categories);
        map.put("ds", dataStatistics);
        return map;
    }
}
