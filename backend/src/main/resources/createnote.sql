
-- 用户表 - 存储用户基本信息
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '用户邮箱，唯一',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_email (email) COMMENT '邮箱索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 分类表 - 存储便签分类
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID，主键',
    user_id INT NOT NULL COMMENT '用户ID，逻辑关联users表',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    color VARCHAR(20) DEFAULT '#6a5acd' COMMENT '分类颜色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='便签分类表';

-- 便签表 - 存储便签内容
CREATE TABLE notes (
    note_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '便签ID，主键',
    user_id INT NOT NULL COMMENT '用户ID，逻辑关联users表',
    title VARCHAR(100) NOT NULL COMMENT '便签标题',
    content TEXT COMMENT '便签内容',
    color VARCHAR(20) DEFAULT '#ffffff' COMMENT '便签颜色',
    category_id INT COMMENT '分类ID，逻辑关联categories表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_pinned BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_archived BOOLEAN DEFAULT FALSE COMMENT '是否归档',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引',
    INDEX idx_category_id (category_id) COMMENT '分类ID索引',
    INDEX idx_pinned_archived (is_pinned, is_archived) COMMENT '置顶和归档状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='便签内容表';
