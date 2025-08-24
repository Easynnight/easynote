package com.example.backend.mapper;

import com.example.backend.model.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    List<Category> findByUserId(Integer userId);
    Category findById(Integer categoryId);
    int insert(Category category);
    int update(Category category);
    int delete(Integer categoryId);
}