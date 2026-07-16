-- H2 Database Schema
CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    gender VARCHAR(10) DEFAULT '0',
    height VARCHAR(10) DEFAULT NULL,
    weight VARCHAR(10) DEFAULT NULL,
    body_type VARCHAR(20) DEFAULT NULL,
    skin_tone VARCHAR(20) DEFAULT NULL,
    style_pref VARCHAR(100) DEFAULT NULL,
    color_pref VARCHAR(100) DEFAULT NULL,
    fashion_ban VARCHAR(500) DEFAULT NULL,
    full_body_photo VARCHAR(500) DEFAULT NULL,
    security_question VARCHAR(200) DEFAULT NULL,
    security_answer VARCHAR(200) DEFAULT NULL,
    city VARCHAR(50) DEFAULT NULL,
    role INT DEFAULT 0,
    status INT DEFAULT 1,
    daily_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outfit_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100) DEFAULT NULL,
    scheme_index INT DEFAULT NULL,
    user_input TEXT,
    weather_info VARCHAR(500) DEFAULT NULL,
    parsed_params TEXT DEFAULT NULL,
    prompt TEXT,
    image_url VARCHAR(1000) DEFAULT NULL,
    style_tags VARCHAR(200) DEFAULT NULL,
    top_item VARCHAR(200) DEFAULT NULL,
    bottom_item VARCHAR(200) DEFAULT NULL,
    shoes_item VARCHAR(200) DEFAULT NULL,
    accessory_item VARCHAR(200) DEFAULT NULL,
    match_reason TEXT DEFAULT NULL,
    score_body INT DEFAULT NULL,
    score_weather INT DEFAULT NULL,
    score_occasion INT DEFAULT NULL,
    score_color INT DEFAULT NULL,
    score_total INT DEFAULT NULL,
    optimize_advice TEXT DEFAULT NULL,
    status INT DEFAULT 0,
    fail_reason VARCHAR(500) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS clothes_collect (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, record_id)
);

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(1000) DEFAULT NULL,
    config_desc VARCHAR(200) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS conversation_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(100) NOT NULL,
    user_id BIGINT DEFAULT NULL,
    context_json TEXT DEFAULT NULL,
    current_scheme INT DEFAULT NULL,
    expire_time DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT DEFAULT NULL,
    log_type INT DEFAULT NULL,
    operation VARCHAR(500) DEFAULT NULL,
    request_param TEXT DEFAULT NULL,
    result TEXT DEFAULT NULL,
    ip VARCHAR(50) DEFAULT NULL,
    cost_time INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);