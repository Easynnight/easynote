package com.example.backend.controller;

import com.example.backend.model.Category;
import com.example.backend.model.User;
import com.example.backend.service.CategoryService;
import com.example.backend.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LogManager.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 获取当前用户的所有分类
    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        logger.debug("开始获取当前用户的所有分类");
        Integer userId = getCurrentUserId();
        List<Category> categories = categoryService.getCategoriesByUserId(userId);
        logger.debug("成功获取{}个分类", categories.size());
        return ResponseEntity.ok(categories);
    }

    // 根据ID获取分类
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        logger.debug("开始获取ID为{}的分类", id);
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            logger.warn("未找到ID为{}的分类", id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证是否为当前用户的分类
        Integer userId = getCurrentUserId();
        if (!category.getUserId().equals(userId)) {
            logger.warn("用户{}尝试访问不属于自己的分类{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        logger.debug("成功获取分类: {}", category.getName());
        return ResponseEntity.ok(category);
    }

    // 创建新分类
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        logger.debug("开始创建新分类: {}", category.getName());
        Integer userId = getCurrentUserId();
        category.setUserId(userId);
        
        boolean created = categoryService.createCategory(category);
        if (!created) {
            logger.error("创建分类失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功创建分类: {}", category.getName());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("category", category);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 更新分类
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        logger.debug("开始更新分类: {}", id);
        category.setCategoryId(id);
        
        // 验证是否为当前用户的分类
        Category existingCategory = categoryService.getCategoryById(id);
        if (existingCategory == null) {
            logger.warn("未找到ID为{}的分类", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer userId = getCurrentUserId();
        if (!existingCategory.getUserId().equals(userId)) {
            logger.warn("用户{}尝试更新不属于自己的分类{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean updated = categoryService.updateCategory(category);
        if (!updated) {
            logger.error("更新分类失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功更新分类: {}", id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("category", category);
        return ResponseEntity.ok(response);
    }

    // 删除分类
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Integer id) {
        logger.debug("开始删除分类: {}", id);
        
        // 验证是否为当前用户的分类
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            logger.warn("未找到ID为{}的分类", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer userId = getCurrentUserId();
        if (!category.getUserId().equals(userId)) {
            logger.warn("用户{}尝试删除不属于自己的分类{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean deleted = categoryService.deleteCategory(id);
        if (!deleted) {
            logger.error("删除分类失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功删除分类: {}", id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // 获取当前登录用户的ID
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            // 从认证对象中获取用户名
            String username = authentication.getName();
            // 根据用户名查找用户信息
            User user = userService.findByUsername(username);
            if (user != null) {
                // 将Long类型的id转换为Integer类型
                return user.getId().intValue();
            }
        }
        // 认证失败或用户不存在时返回默认值
        logger.warn("无法获取当前用户ID，返回默认值1");
        return 1;
    }

    @Autowired
    private UserService userService;
}