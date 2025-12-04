package com.iblog.service;

import com.iblog.bean.Category;
import com.iblog.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author maple
 * @Description
 * @createTime:2025-12-04 11:33
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CategoryService {
    private final CategoryMapper categoryMapper;

    public List<Category> getAllCategories() {
        log.debug("getAllCategories called");
        return categoryMapper.getAllCategories();
    }

    public boolean deleteCategoryByIds(String ids) {
        log.debug("deleteCategoryByIds called: ids={}", ids);
        String[] split = ids.split(",");
        int result = categoryMapper.deleteCategoryByIds(split);
        log.debug("deleteCategoryByIds result: {} expected: {}", result, split.length);
        return result == split.length;
    }

    public int updateCategoryById(Category category) {
        log.debug("updateCategoryById called: {}", category == null ? "null" : category.getId());
        return categoryMapper.updateCategoryById(category);
    }

    public int addCategory(Category category) {
        log.debug("addCategory called: {}", category == null ? "null" : category.getCateName());
        if (category != null) {
            category.setDate(LocalDateTime.now());
        }
        int i = categoryMapper.addCategory(category);
        log.debug("addCategory 返回: {}", i);
        return i;
    }
}
