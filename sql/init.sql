-- ============================================
-- AI智能穿搭生成系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS ai_outfit DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_outfit;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    
ickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    vatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未设置 1-男 2-女',
    height INT DEFAULT NULL COMMENT '身高(cm)',
    weight INT DEFAULT NULL COMMENT '体重(kg)',
    ody_type VARCHAR(20) DEFAULT NULL COMMENT '身材：小个子、标准身材、梨形、苹果型、微胖',
    skin_tone VARCHAR(20) DEFAULT NULL COMMENT '肤色：冷白皮、黄一白、黄黑皮',
    style_pref VARCHAR(100) DEFAULT NULL COMMENT '风格偏好(逗号分隔)：通勤、温柔、甜酷、国风、运动、休闲、复古、礼服',
    color_pref VARCHAR(100) DEFAULT NULL COMMENT '色系偏好(逗号分隔)：浅色系、深色系、莫兰迪、冷色调、暖色调',
    city VARCHAR(50) DEFAULT NULL COMMENT '常驻城市',
    ole TINYINT DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (ole),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- AI穿搭记录表
CREATE TABLE IF NOT EXISTS outfit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_input TEXT COMMENT '用户原始话术',
    weather_info VARCHAR(500) DEFAULT NULL COMMENT '天气信息(JSON)',
    parsed_params JSON DEFAULT NULL COMMENT 'AI解析的结构化穿搭参数(JSON)',
    prompt TEXT COMMENT '生成的绘图Prompt',
    image_url VARCHAR(1000) DEFAULT NULL COMMENT '穿搭图片URL',
    style_tags VARCHAR(200) DEFAULT NULL COMMENT '风格标签',
    status TINYINT DEFAULT 0 COMMENT '状态：0-生成中 1-成功 2-失败',
    ail_reason VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI穿搭记录表';

-- 穿搭收藏表
CREATE TABLE IF NOT EXISTS outfit_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    ecord_id BIGINT NOT NULL COMMENT '穿搭记录ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_record (user_id, ecord_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='穿搭收藏表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(1000) DEFAULT NULL COMMENT '配置值',
    description VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('ai_model_api_key', '', '大模型API密钥'),
('ai_model_api_url', 'https://api.coze.cn/open_api/v2/chat', '大模型API地址'),
('ai_daily_limit', '100', '每日AI生成次数限制'),
('image_gen_api_key', '', '图片生成API密钥'),
('image_gen_api_url', '', '图片生成API地址'),
('weather_api_key', '', '天气API密钥'),
('weather_api_url', 'https://restapi.amap.com/v3/weather/weatherInfo', '天气API地址'),
('system_name', 'AI智能穿搭生成系统', '系统名称');

-- 插入默认管理员账号(密码: admin123)
INSERT INTO user (username, password, 
ickname, ole, status) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, 1);
