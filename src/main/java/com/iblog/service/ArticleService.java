package com.iblog.service;

import com.iblog.bean.Article;
import com.iblog.bean.User;
import com.iblog.mapper.ArticleMapper;
import com.iblog.mapper.TagsMapper;
import com.iblog.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author maple
 * @Description
 * @createTime:2025-11-23 20:21
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final TagsMapper tagsMapper;

    public int addNewArticle(Article article) {
        log.debug("addNewArticle 开始, article={}", Util.toJson(article));
        //处理文章摘要
        if (article.getSummary() == null || article.getSummary().isEmpty()) {
            //直接截取
            String stripHtml = stripHtml(article.getHtmlContent());
            article.setSummary(stripHtml.substring(0, Math.min(stripHtml.length(), 50)));
        }
        if (article.getId() == -1) {
            //添加操作
            LocalDateTime timestamp = LocalDateTime.now();
            if (article.getState() == 1) {
                //设置发表日期
                article.setPublishDate(timestamp);
            }
            article.setEditTime(timestamp);
            //设置当前用户
            article.setUid(Util.getCurrentUser().getId());
            int i = articleMapper.addNewArticle(article);
            log.debug("新增文章入库返回: {}", i);
            //打标签
            String[] dynamicTags = article.getDynamicTags();
            if (dynamicTags != null && dynamicTags.length > 0) {
                int tags = addTagsToArticle(dynamicTags, article.getId());
                if (tags == -1) {
                    log.error("给文章添加标签失败, tagsCount被标记为-1");
                    return tags;
                }
            }
            log.info("新增文章成功: id={}", article.getId());
            return i;
        } else {
            LocalDateTime timestamp = LocalDateTime.now();
            if (article.getState() == 1) {
                //设置发表日期
                article.setPublishDate(timestamp);
            }
            //更新
            article.setEditTime(LocalDateTime.now());
            int i = articleMapper.updateArticle(article);
            log.debug("更新文章返回: {}", i);
            //修改标签
            String[] dynamicTags = article.getDynamicTags();
            if (dynamicTags != null && dynamicTags.length > 0) {
                int tags = addTagsToArticle(dynamicTags, article.getId());
                if (tags == -1) {
                    log.error("更新文章时给文章添加标签失败");
                    return tags;
                }
            }
            log.info("文章更新成功: id={}", article.getId());
            return i;
        }
    }

    private int addTagsToArticle(String[] dynamicTags, Long aid) {
        log.debug("addTagsToArticle: tags={}, aid={}", Util.toJson(dynamicTags), aid);
        //1.删除该文章目前所有的标签
        tagsMapper.deleteTagsByAid(aid);
        //2.将上传上来的标签全部存入数据库
        tagsMapper.saveTags(dynamicTags);
        //3.查询这些标签的id
        List<Long> tIds = tagsMapper.getTagsIdByTagName(dynamicTags);
        //4.重新给文章设置标签
        int i = tagsMapper.saveTags2ArticleTags(tIds, aid);
        log.debug("标签关联返回: expected={}, actual={}", dynamicTags.length, i);
        return i == dynamicTags.length ? i : -1;
    }

    public String stripHtml(String content) {
        content = content.replaceAll("<p .*?>", "");
        content = content.replaceAll("<br\\s*/?>", "");
        content = content.replaceAll("<.*?>", "");
        return content;
    }

    public List<Article> getArticleByState(Integer state, Integer page, Integer count,String keywords) {
        int start = (page - 1) * count;
        java.lang.Long uid = null;
        User u = Util.getCurrentUser();
        if (u != null) uid = u.getId();
        log.debug("getArticleByState: state={}, start={}, count={}, uid={}, keywords={}", state, start, count, uid, keywords);
        return articleMapper.getArticleByState(state, start, count, uid, keywords);
    }

    /**
     * 公共/按指定 uid 查询文章（uid 可为 null 表示不按用户过滤）
     */
    public List<Article> getArticleByStateForUid(Integer state, Integer page, Integer count, String keywords, Long uid) {
        int start = (page - 1) * count;
        log.debug("getArticleByStateForUid: state={}, start={}, count={}, uid={}, keywords={}", state, start, count, uid, keywords);
        return articleMapper.getArticleByState(state, start, count, uid, keywords);
    }

    public int getArticleCountByStateForUid(Integer state, Long uid, String keywords) {
        log.debug("getArticleCountByStateForUid: state={}, uid={}, keywords={}", state, uid, keywords);
        return articleMapper.getArticleCountByState(state, uid, keywords);
    }


    public int getArticleCountByState(Integer state, Long uid,String keywords) {
        return articleMapper.getArticleCountByState(state, uid,keywords);
    }

    public int updateArticleState(List<Long> aids, Integer state) {
        log.debug("updateArticleState: aids={}, state={}", Util.toJson(aids), state);
        int i = 0;
        if (state == 2) {
            // delete
            i = articleMapper.deleteArticleById(aids);
            log.info("删除文章返回: {}", i);
        } else {
            // move to dustbin
            i = articleMapper.updateArticleState(aids, 2);
            log.info("文章移动到回收站返回: {}", i);
        }
        return i;
    }

    public Article getArticleById(Long aid) {
        log.debug("getArticleById: aid={}", aid);
        Article article = articleMapper.getArticleById(aid);
        articleMapper.pvIncrement(aid);
        log.debug("getArticleById 返回 article={}", Util.toJson(article));
        return article;
    }

    /**
     * 获取文章所属用户ID（不变更 pv）
     */
    public Long getArticleOwnerId(Long aid) {
        Article article = articleMapper.getArticleById(aid);
        if (article == null) return null;
        return article.getUid();
    }

    public void pvStatisticsPerDay() {
        log.info("pvStatisticsPerDay 开始");
        articleMapper.pvStatisticsPerDay();
        log.info("pvStatisticsPerDay 完成");
    }

    /**
     * 获取最近七天的日期
     */
    public List<String> getCategories() {
        User u = Util.getCurrentUser();
        if (u == null || u.getId() == null) {
            log.debug("getCategories called for anonymous user, returning empty list");
            return java.util.Collections.emptyList();
        }
        log.debug("getCategories called for uid={}", u.getId());
        return articleMapper.getCategories(u.getId());
    }

    /**
     * 获取最近七天的数据
     */
    public List<Integer> getDataStatistics() {
        User u = Util.getCurrentUser();
        if (u == null || u.getId() == null) {
            log.debug("getDataStatistics called for anonymous user, returning empty list");
            return java.util.Collections.emptyList();
        }
        log.debug("getDataStatistics called for uid={}", u.getId());
        return articleMapper.getDataStatistics(u.getId());
    }
}
