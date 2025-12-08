package com.iblog.controller;

import com.iblog.bean.Category;
import com.iblog.bean.RespBean;
import com.iblog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author maple
 * @Description
 * @createTime:2025-12-04 11:50
 */
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Category> getAllCategories() {
        log.debug("获取所有分类请求");
        return categoryService.getAllCategories();
    }

    @RequestMapping(value = "/{ids}", method = RequestMethod.DELETE)
    public RespBean deleteById(@PathVariable String ids) {
        log.debug("删除分类请求: ids={}", ids);
        boolean result = categoryService.deleteCategoryByIds(ids);
        if (result) {
            log.info("删除分类成功: ids={}", ids);
            return new RespBean("success", "删除成功!");
        }
        log.warn("删除分类失败: ids={}", ids);
        return new RespBean("error", "删除失败!");
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public RespBean addNewCate(Category category) {
        log.debug("添加分类请求: {}", category == null ? "null" : category.getCateName());
        int result = categoryService.addCategory(category);
        if (result == 1) {
            log.info("添加分类成功: id={}, name={}", category != null ? category.getId() : null, category != null ? category.getCateName() : null);
            return new RespBean("success", "添加成功!");
        }
        log.warn("添加分类失败: name={}", category != null ? category.getCateName() : null);
        return new RespBean("error", "添加失败!");
    }

    @RequestMapping(value = "/", method = RequestMethod.PUT)
    public RespBean updateCate(Category category) {
        log.debug("更新分类请求: {}", category == null ? "null" : category.getId());
        int i = categoryService.updateCategoryById(category);
        if (i == 1) {
            log.info("更新分类成功: id={}", category != null ? category.getId() : null);
            return new RespBean("success", "修改成功!");
        }
        log.warn("更新分类失败: id={}", category == null ? null : category.getId());
        return new RespBean("error", "修改失败!");
    }
}
