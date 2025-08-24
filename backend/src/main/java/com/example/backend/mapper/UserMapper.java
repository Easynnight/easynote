package com.example.backend.mapper;

import com.example.backend.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findByEmail(@Param("email") String email);
    User findById(@Param("id") Long id);
    int insert(User user);
    int update(User user);
    int delete(@Param("id") Long id);
}