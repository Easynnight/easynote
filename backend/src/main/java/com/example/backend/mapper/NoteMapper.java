package com.example.backend.mapper;

import com.example.backend.model.Note;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface NoteMapper {
    List<Note> findByUserId(Map<String, Object> params);
    List<Note> findByUserIdAndCategoryId(Integer userId, Integer categoryId);
    Note findById(Integer noteId);
    List<Note> searchNotes(Map<String, Object> params);
    int insert(Note note);
    int update(Note note);
    int delete(Integer noteId);
    int pinNote(Integer noteId, Boolean isPinned);
    int archiveNote(Integer noteId, Boolean isArchived);
}