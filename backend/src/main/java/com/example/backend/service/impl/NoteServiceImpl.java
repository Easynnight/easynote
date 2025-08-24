package com.example.backend.service.impl;

import com.example.backend.mapper.NoteMapper;
import com.example.backend.model.Note;
import com.example.backend.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteMapper noteMapper;

    @Override
    public List<Note> getNotesByUserId(Integer userId, Boolean isArchived) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("isArchived", isArchived);
        return noteMapper.findByUserId(params);
    }

    @Override
    public List<Note> getNotesByUserIdAndCategoryId(Integer userId, Integer categoryId, Boolean isArchived) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("categoryId", categoryId);
        params.put("isArchived", isArchived);
        return noteMapper.findByUserIdAndCategoryId(userId, categoryId);
    }

    @Override
    public Note getNoteById(Integer noteId) {
        return noteMapper.findById(noteId);
    }

    @Override
    public List<Note> searchNotes(Map<String, Object> params) {
        return noteMapper.searchNotes(params);
    }

    @Override
    public boolean createNote(Note note) {
        return noteMapper.insert(note) > 0;
    }

    @Override
    public boolean updateNote(Note note) {
        return noteMapper.update(note) > 0;
    }

    @Override
    public boolean deleteNote(Integer noteId) {
        return noteMapper.delete(noteId) > 0;
    }

    @Override
    public boolean pinNote(Integer noteId, Boolean isPinned) {
        Map<String, Object> params = new HashMap<>();
        params.put("noteId", noteId);
        params.put("isPinned", isPinned);
        return noteMapper.pinNote(noteId, isPinned) > 0;
    }

    @Override
    public boolean archiveNote(Integer noteId, Boolean isArchived) {
        Map<String, Object> params = new HashMap<>();
        params.put("noteId", noteId);
        params.put("isArchived", isArchived);
        return noteMapper.archiveNote(noteId, isArchived) > 0;
    }
}