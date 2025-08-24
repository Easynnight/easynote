package com.example.backend.service;

import com.example.backend.model.Note;

import java.util.List;
import java.util.Map;

public interface NoteService {
    List<Note> getNotesByUserId(Integer userId, Boolean isArchived);
    List<Note> getNotesByUserIdAndCategoryId(Integer userId, Integer categoryId, Boolean isArchived);
    Note getNoteById(Integer noteId);
    List<Note> searchNotes(Map<String, Object> params);
    boolean createNote(Note note);
    boolean updateNote(Note note);
    boolean deleteNote(Integer noteId);
    boolean pinNote(Integer noteId, Boolean isPinned);
    boolean archiveNote(Integer noteId, Boolean isArchived);
}