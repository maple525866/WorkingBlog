package com.iblog.controller;

import com.iblog.bean.Category;
import com.iblog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 向普通用户公开的分类接口（只提供读取分类列表）
 */
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {

    private final CategoryService categoryService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Category> getAllCategoriesPublic() {
        log.debug("public getAllCategories called");
        return categoryService.getAllCategories();
    }
}
