package com.example.backend.controller;

import com.example.backend.model.Note;
import com.example.backend.model.Category;
import com.example.backend.model.User;
import com.example.backend.service.NoteService;
import com.example.backend.service.CategoryService;
import com.example.backend.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
public class NoteExportController {

    private static final Logger logger = LogManager.getLogger(NoteExportController.class);
    private final NoteService noteService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public NoteExportController(NoteService noteService, CategoryService categoryService, UserService userService) {
        this.noteService = noteService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    /**
     * 导出所有笔记到Word文档
     */
    @GetMapping("/notes/word")
    public ResponseEntity<byte[]> exportNotesToWord() {
        logger.debug("开始导出所有笔记到Word文档");
        try {
            // 获取当前用户的ID
            Integer userId = getCurrentUserId();
            if (userId == null) {
                logger.error("无法获取当前用户信息");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // 获取当前用户的所有笔记（包括已归档的）
            List<Note> notes = noteService.getNotesByUserId(userId, false);
            List<Note> archivedNotes = noteService.getNotesByUserId(userId, true);
            notes.addAll(archivedNotes);
            
            logger.debug("成功获取{}个笔记用于导出", notes.size());

            // 获取所有分类信息，用于在文档中显示
            List<Category> categories = categoryService.getCategoriesByUserId(userId);
            Map<Integer, String> categoryMap = new HashMap<>();
            for (Category category : categories) {
                categoryMap.put(category.getCategoryId(), category.getName());
            }

            // 创建Word文档
            byte[] documentBytes = createWordDocument(notes, categoryMap);

            // 设置HTTP响应头，使浏览器能够下载文件
            HttpHeaders headers = new HttpHeaders();
            String fileName = "我的笔记导出_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".docx";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            logger.debug("成功生成Word文档，大小：{}字节", documentBytes.length);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(documentBytes);
        } catch (Exception e) {
            logger.error("导出笔记到Word文档时发生错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 创建Word文档并写入笔记内容
     */
    private byte[] createWordDocument(List<Note> notes, Map<Integer, String> categoryMap) throws IOException {
        // 创建新的Word文档
        XWPFDocument document = new XWPFDocument();
        
        // 添加文档标题
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("我的笔记汇总");
        titleRun.setFontSize(24);
        titleRun.setBold(true);
        
        // 添加文档生成日期
        XWPFParagraph dateParagraph = document.createParagraph();
        dateParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun dateRun = dateParagraph.createRun();
        dateRun.setText("生成日期：" + new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date()));
        dateRun.setFontSize(12);
        dateRun.setColor("666666");
        
        // 添加空行
        document.createParagraph();
        
        // 如果没有笔记，添加提示信息
        if (notes.isEmpty()) {
            XWPFParagraph emptyParagraph = document.createParagraph();
            XWPFRun emptyRun = emptyParagraph.createRun();
            emptyRun.setText("没有可导出的笔记");
            emptyRun.setFontSize(14);
        } else {
            // 添加笔记数量统计
            XWPFParagraph countParagraph = document.createParagraph();
            XWPFRun countRun = countParagraph.createRun();
            countRun.setText("共包含" + notes.size() + "个笔记\n");
            countRun.setFontSize(14);
            countRun.setBold(true);
            
            // 按创建时间倒序排序笔记
            notes.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
            // 遍历所有笔记并添加到文档中
            for (Note note : notes) {
                // 添加笔记标题
                XWPFParagraph noteTitleParagraph = document.createParagraph();
                noteTitleParagraph.setPageBreak(true); // 每个笔记单独一页
                XWPFRun noteTitleRun = noteTitleParagraph.createRun();
                noteTitleRun.setText(note.getTitle() != null && !note.getTitle().isEmpty() ? note.getTitle() : "无标题笔记");
                noteTitleRun.setFontSize(16);
                noteTitleRun.setBold(true);
                
                // 添加笔记元信息（分类、创建时间等）
                XWPFParagraph metaParagraph = document.createParagraph();
                XWPFRun metaRun = metaParagraph.createRun();
                
                String metaInfo = "创建时间：" + formatDate(note.getCreatedAt());
                if (note.getCategoryId() != null && categoryMap.containsKey(note.getCategoryId())) {
                    metaInfo += " | 分类：" + categoryMap.get(note.getCategoryId());
                }
                if (note.getIsArchived() != null && note.getIsArchived()) {
                    metaInfo += " | 已归档";
                }
                
                metaRun.setText(metaInfo);
                metaRun.setFontSize(10);
                metaRun.setColor("999999");
                
                // 添加空行
                document.createParagraph();
                
                // 添加笔记内容
                XWPFParagraph contentParagraph = document.createParagraph();
                XWPFRun contentRun = contentParagraph.createRun();
                contentRun.setText(note.getContent() != null ? note.getContent() : "无内容");
                contentRun.setFontSize(12);
                contentRun.addCarriageReturn(); // 确保内容后有空行
            }
        }
        
        // 将文档写入字节数组
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();
        out.close();
        
        return out.toByteArray();
    }

    /**
     * 格式化日期为易读格式
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "未知";
        }
        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(date);
    }

    /**
     * 获取当前登录用户的ID
     */
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
            logger.warn("用户名为{}的用户不存在", username);
        }
        // 认证失败或用户不存在时返回null
        logger.warn("无法获取当前用户ID");
        return null;
    }
}