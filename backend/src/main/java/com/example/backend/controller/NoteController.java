package com.example.backend.controller;

import com.example.backend.model.Note;
import com.example.backend.model.User;
import com.example.backend.service.NoteService;
import com.example.backend.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private static final Logger logger = LogManager.getLogger(NoteController.class);
    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // 获取当前用户的所有便签
    @GetMapping
    public ResponseEntity<List<Note>> getNotes(
            @RequestParam(value = "archived", required = false, defaultValue = "false") Boolean isArchived) {
        logger.debug("开始获取当前用户的便签，归档状态: {}", isArchived);
        Integer userId = getCurrentUserId();
        List<Note> notes = noteService.getNotesByUserId(userId, isArchived);
        logger.debug("成功获取{}个便签", notes.size());
        return ResponseEntity.ok(notes);
    }

    // 根据分类获取便签
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Note>> getNotesByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(value = "archived", required = false, defaultValue = "false") Boolean isArchived) {
        logger.debug("开始获取当前用户分类ID为{}的便签，归档状态: {}", categoryId, isArchived);
        Integer userId = getCurrentUserId();
        List<Note> notes = noteService.getNotesByUserIdAndCategoryId(userId, categoryId, isArchived);
        logger.debug("成功获取{}个便签", notes.size());
        return ResponseEntity.ok(notes);
    }

    // 根据ID获取便签
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Integer id) {
        logger.debug("开始获取ID为{}的便签", id);
        Note note = noteService.getNoteById(id);
        if (note == null) {
            logger.warn("未找到ID为{}的便签", id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证是否为当前用户的便签
        Integer userId = getCurrentUserId();
        if (!note.getUserId().equals(userId)) {
            logger.warn("用户{}尝试访问不属于自己的便签{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        logger.debug("成功获取便签: {}", note.getTitle());
        return ResponseEntity.ok(note);
    }

    // 搜索便签
    @GetMapping("/search")
    public ResponseEntity<List<Note>> searchNotes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false, defaultValue = "false") Boolean isArchived) {
        logger.debug("开始搜索便签: 关键词={}, 分类ID={}, 归档状态={}", keyword, categoryId, isArchived);
        Integer userId = getCurrentUserId();
        
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("keyword", keyword);
        params.put("categoryId", categoryId);
        params.put("isArchived", isArchived);
        
        List<Note> notes = noteService.searchNotes(params);
        logger.debug("搜索结果: {}个便签", notes.size());
        return ResponseEntity.ok(notes);
    }

    // 创建新便签
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNote(@RequestBody Note note) {
        logger.debug("开始创建新便签: {}", note.getTitle());
        Integer userId = getCurrentUserId();
        note.setUserId(userId);
        note.setCreatedAt(new Date());
        note.setUpdatedAt(new Date());
        
        if (note.getIsPinned() == null) {
            note.setIsPinned(false);
        }
        if (note.getIsArchived() == null) {
            note.setIsArchived(false);
        }
        
        boolean created = noteService.createNote(note);
        if (!created) {
            logger.error("创建便签失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功创建便签: {}", note.getTitle());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("note", note);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 更新便签
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNote(@PathVariable Integer id, @RequestBody Note note) {
        logger.debug("开始更新便签: {}", id);
        note.setNoteId(id);
        note.setUpdatedAt(new Date());
        
        // 验证是否为当前用户的便签
        Note existingNote = noteService.getNoteById(id);
        if (existingNote == null) {
            logger.warn("未找到ID为{}的便签", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer userId = getCurrentUserId();
        if (!existingNote.getUserId().equals(userId)) {
            logger.warn("用户{}尝试更新不属于自己的便签{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // 保留原始创建时间
        note.setCreatedAt(existingNote.getCreatedAt());
        
        boolean updated = noteService.updateNote(note);
        if (!updated) {
            logger.error("更新便签失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功更新便签: {}", id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("note", note);
        return ResponseEntity.ok(response);
    }

    // 删除便签
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNote(@PathVariable Integer id) {
        logger.debug("开始删除便签: {}", id);
        
        // 验证是否为当前用户的便签
        Note note = noteService.getNoteById(id);
        if (note == null) {
            logger.warn("未找到ID为{}的便签", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer userId = getCurrentUserId();
        if (!note.getUserId().equals(userId)) {
            logger.warn("用户{}尝试删除不属于自己的便签{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean deleted = noteService.deleteNote(id);
        if (!deleted) {
            logger.error("删除便签失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功删除便签: {}", id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    // 置顶/取消置顶便签
    @PutMapping("/{id}/pin")
    public ResponseEntity<Map<String, Object>> pinNote(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        logger.debug("开始置顶/取消置顶便签: {}", id);
        Boolean isPinned = request.get("isPinned");
        if (isPinned == null) {
            logger.error("请求参数isPinned不能为空");
            return ResponseEntity.badRequest().build();
        }
        
        // 验证是否为当前用户的便签
        Note note = noteService.getNoteById(id);
        if (note == null) {
            logger.warn("未找到ID为{}的便签", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer userId = getCurrentUserId();
        if (!note.getUserId().equals(userId)) {
            logger.warn("用户{}尝试置顶/取消置顶不属于自己的便签{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean updated = noteService.pinNote(id, isPinned);
        if (!updated) {
            logger.error("置顶/取消置顶便签失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功置顶/取消置顶便签: {}, isPinned={}", id, isPinned);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isPinned", isPinned);
        return ResponseEntity.ok(response);
    }

    // 归档/取消归档便签
    @PutMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> archiveNote(@PathVariable Integer id, @RequestBody Map<String, Boolean> request) {
        logger.debug("开始归档/取消归档便签: {}", id);
        Boolean isArchived = request.get("isArchived");
        if (isArchived == null) {
            logger.error("请求参数isArchived不能为空");
            return ResponseEntity.badRequest().build();
        }
        
        // 验证是否为当前用户的便签
        Note note = noteService.getNoteById(id);
        if (note == null) {
            logger.warn("未找到ID为{}的便签", id);
            return ResponseEntity.notFound().build();
        }
        
        Integer userId = getCurrentUserId();
        if (!note.getUserId().equals(userId)) {
            logger.warn("用户{}尝试归档/取消归档不属于自己的便签{}", userId, id);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        boolean updated = noteService.archiveNote(id, isArchived);
        if (!updated) {
            logger.error("归档/取消归档便签失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        logger.debug("成功归档/取消归档便签: {}, isArchived={}", id, isArchived);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("isArchived", isArchived);
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