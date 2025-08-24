-- 增加便签内容字段的容量
ALTER TABLE notes MODIFY COLUMN content LONGTEXT COMMENT '便签内容';

-- 验证修改结果
DESC notes; -- 显示表结构，确认content字段类型已更改为LONGTEXT