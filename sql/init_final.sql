-- AI智能穿搭生成系统 V2.1 最终版数据库
CREATE DATABASE IF NOT EXISTS ai_outfit CHARACTER SET utf8mb4;
USE ai_outfit;

-- 用户表(含穿搭禁忌+密保)
DROP TABLE IF EXISTS user;
CREATE TABLE user (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
username VARCHAR(50) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
nickname VARCHAR(50) DEFAULT NULL,
avatar VARCHAR(500) DEFAULT NULL,
gender VARCHAR(10) DEFAULT NULL COMMENT '男/女',
height VARCHAR(20) DEFAULT NULL,
weight VARCHAR(20) DEFAULT NULL,
body_type VARCHAR(20) DEFAULT NULL,
skin_tone VARCHAR(20) DEFAULT NULL,
style_pref VARCHAR(200) DEFAULT NULL,
color_pref VARCHAR(200) DEFAULT NULL,
fashion_ban VARCHAR(500) DEFAULT NULL COMMENT '穿搭禁忌',
security_question VARCHAR(100) DEFAULT NULL COMMENT '密保问题',
security_answer VARCHAR(100) DEFAULT NULL COMMENT '密保答案',
city VARCHAR(50) DEFAULT NULL,
role TINYINT DEFAULT 0,
status TINYINT DEFAULT 1,
daily_count INT DEFAULT 0 COMMENT '当日生成次数',
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX idx_uname (username),
INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 穿搭记录表(单套方案)
DROP TABLE IF EXISTS outfit_record;
CREATE TABLE outfit_record (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
user_id BIGINT NOT NULL,
session_id VARCHAR(64) DEFAULT NULL,
scheme_index TINYINT DEFAULT 1 COMMENT '方案序号1/2/3',
user_input TEXT,
weather_info VARCHAR(500) DEFAULT NULL,
parsed_params JSON DEFAULT NULL,
prompt TEXT,
image_url VARCHAR(1000) DEFAULT NULL,
style_tags VARCHAR(200) DEFAULT NULL,
top_item VARCHAR(200) DEFAULT NULL,
bottom_item VARCHAR(200) DEFAULT NULL,
shoes_item VARCHAR(200) DEFAULT NULL,
accessory_item VARCHAR(200) DEFAULT NULL,
match_reason TEXT COMMENT '搭配理由',
score_body INT DEFAULT 0,
score_weather INT DEFAULT 0,
score_occasion INT DEFAULT 0,
score_color INT DEFAULT 0,
score_total INT DEFAULT 0,
optimize_advice TEXT COMMENT '优化建议',
status TINYINT DEFAULT 0,
fail_reason VARCHAR(500) DEFAULT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
INDEX idx_uid (user_id),
INDEX idx_sid (session_id),
INDEX idx_st (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收藏表
DROP TABLE IF EXISTS clothes_collect;
CREATE TABLE clothes_collect (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
user_id BIGINT NOT NULL,
record_id BIGINT NOT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY uk_ur (user_id,record_id),
INDEX idx_uid (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 对话会话表
DROP TABLE IF EXISTS conversation_session;
CREATE TABLE conversation_session (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
session_id VARCHAR(64) NOT NULL UNIQUE,
user_id BIGINT NOT NULL,
context_json JSON DEFAULT NULL,
current_scheme TINYINT DEFAULT 1,
expire_time DATETIME DEFAULT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
INDEX idx_sid (session_id),
INDEX idx_uid (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统配置表
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
config_key VARCHAR(50) NOT NULL UNIQUE,
config_value VARCHAR(500) DEFAULT NULL,
config_desc VARCHAR(255) DEFAULT NULL,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
user_id BIGINT DEFAULT NULL,
log_type TINYINT DEFAULT 1 COMMENT '1-用户操作 2-AI调用',
operation VARCHAR(200) DEFAULT NULL,
request_param TEXT,
result VARCHAR(500) DEFAULT NULL,
ip VARCHAR(50) DEFAULT NULL,
cost_time INT DEFAULT 0,
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
INDEX idx_uid (user_id),
INDEX idx_type (log_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始数据
INSERT INTO sys_config (config_key,config_value,config_desc) VALUES
('ai_model_api_key','','大模型API密钥'),
('ai_model_api_url','https://api.coze.cn/open_api/v2/chat','大模型API地址'),
('user_daily_limit','10','用户每日生成次数限制'),
('global_rate_limit','5','全局每秒调用频率限制'),
('weather_api_key','','高德天气API密钥'),
('weather_api_url','https://restapi.amap.com/v3/weather/weatherInfo','天气API地址'),
('session_expire_minutes','30','会话过期时间(分钟)'),
('system_name','AI智能穿搭生成系统','系统名称');

INSERT INTO user (username,password,nickname,role,status) VALUES
('admin','e10adc3949ba59abbe56e057f20f883e','系统管理员',1,1);
INSERT INTO user (username,password,nickname,gender,body_type,skin_tone,style_pref,color_pref,city,role,status) VALUES
('demo','e10adc3949ba59abbe56e057f20f883e','演示用户','女','标准身材','黄一白','通勤,温柔,休闲','浅色系,莫兰迪','北京',0,1);
