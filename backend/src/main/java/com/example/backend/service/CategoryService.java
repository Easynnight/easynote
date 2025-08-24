package com.example.backend.service;

import com.example.backend.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getCategoriesByUserId(Integer userId);
    Category getCategoryById(Integer categoryId);
    boolean createCategory(Category category);
    boolean updateCategory(Category category);
    boolean deleteCategory(Integer categoryId);
}