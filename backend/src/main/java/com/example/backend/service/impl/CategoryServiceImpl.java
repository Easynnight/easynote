package com.example.backend.service.impl;

import com.example.backend.mapper.CategoryMapper;
import com.example.backend.model.Category;
import com.example.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getCategoriesByUserId(Integer userId) {
        return categoryMapper.findByUserId(userId);
    }

    @Override
    public Category getCategoryById(Integer categoryId) {
        return categoryMapper.findById(categoryId);
    }

    @Override
    public boolean createCategory(Category category) {
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(Category category) {
        return categoryMapper.update(category) > 0;
    }

    @Override
    public boolean deleteCategory(Integer categoryId) {
        return categoryMapper.delete(categoryId) > 0;
    }
}