MERGE INTO sys_config (config_key, config_value, config_desc) KEY(config_key) VALUES
('ai_model_api_key', '', 'AI API Key'),
('ai_model_api_url', 'https://api.coze.cn/open_api/v2/chat', 'AI API URL'),
('ai_daily_limit', '100', 'Daily AI generation limit'),
('image_gen_api_key', '', 'Image generation API Key'),
('image_gen_api_url', '', 'Image generation API URL'),
('weather_api_key', '', 'Weather API Key'),
('weather_api_url', 'https://restapi.amap.com/v3/weather/weatherInfo', 'Weather API URL'),
('system_name', 'AI Outfit System', 'System name');

MERGE INTO app_user (username, password, nickname, role, status) KEY(username) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', 'Admin', 1, 1),
('demo', '827ccb0eea8a706c4c34a16891f84e7b', 'Demo', 0, 1);