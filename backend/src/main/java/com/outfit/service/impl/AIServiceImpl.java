package com.outfit.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.outfit.entity.User;
import com.outfit.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIServiceImpl implements com.outfit.service.AIService {

    @Autowired private SystemConfigMapper configMapper;

    public String generateOutfitPlan(User user, String userInput, String weather, String temp, String city, String sessionContext) {
        String apiKey = configMapper.getValueByKey("ai_model_api_key");
        String apiUrl = configMapper.getValueByKey("ai_model_api_url");
        String systemPrompt = buildSystemPrompt(user, weather, temp, city);
        if (sessionContext != null && !sessionContext.isEmpty()) {
            systemPrompt += "\n历史对话上下文：" + sessionContext;
        }
        String userPrompt = "用户需求：" + userInput;
        if (user.getFashionBan() != null && !user.getFashionBan().isEmpty()) {
            userPrompt += "\n穿搭禁忌（请严格遵守）：" + user.getFashionBan();
        }
        userPrompt += "\n请严格按照上述JSON格式输出3套穿搭方案，用数组包裹。";

        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                return callCoze(apiUrl, apiKey, systemPrompt, userPrompt);
            } catch (Exception e) {
                return generateFallback(user, userInput, weather, temp, city);
            }
        }
        return generateFallback(user, userInput, weather, temp, city);
    }

    private String callCoze(String url, String key, String sys, String user) {
        Map<String,Object> params = new HashMap<>();
        params.put("bot_id", "outfit_bot");
        params.put("user", "user");
        params.put("query", user);
        JSONArray history = new JSONArray();
        history.add(new JSONObject().set("role","system").set("content", sys));
        params.put("chat_history", history);
        String resp = HttpUtil.createPost(url)
            .header("Authorization","Bearer "+key)
            .header("Content-Type","application/json")
            .body(JSONUtil.toJsonStr(params))
            .execute().body();
        JSONObject json = JSONUtil.parseObj(resp);
        if (json.getInt("code") == 0) {
            JSONArray msgs = json.getJSONArray("messages");
            for (Object o : msgs) {
                JSONObject m = (JSONObject) o;
                if ("assistant".equals(m.getStr("role")) || "answer".equals(m.getStr("role"))) {
                    return extractJson(m.getStr("content"));
                }
            }
        }
        throw new RuntimeException("AI call failed");
    }

    private String extractJson(String text) {
        int start = text.indexOf("["); int end = text.lastIndexOf("]");
        if (start != -1 && end != -1 && end > start) return text.substring(start, end+1);
        start = text.indexOf("{"); end = text.lastIndexOf("}");
        if (start != -1 && end != -1) return "[" + text.substring(start, end+1) + "]";
        return text;
    }

    private String buildSystemPrompt(User user, String weather, String temp, String city) {
        String cityInfo = (city != null && !city.isEmpty()) ? city : "未知城市";
        return String.format(
            "你是一个专业AI穿搭师。请严格按以下规则为用户生成3套差异化穿搭方案。\n" +
            "=== 用户档案 ===\n性别：%s\n身材：%s\n肤色：%s\n风格偏好：%s\n色系偏好：%s\n常驻城市：%s\n天气：%s\n温度：%s℃\n\n" +
            "=== 专业穿搭规则 ===\n" +
            "【身材适配】梨形→上紧下松，避雷紧身下装；小个子→高腰款式优化比例；微胖→H版型遮肉；苹果型→修饰腰线\n" +
            "【肤色适配】黄黑皮→避雷荧光色/高饱和冷色，推荐暖调低饱和色系；冷白皮→适配范围广\n" +
            "【天气适配】<15℃→厚外套毛衣等保暖穿搭；15-25℃→薄外套衬衫春秋穿搭；>25℃→短袖轻薄清凉穿搭；雨天→防水外套\n" +
            "【场合】正式场合→三色原则、简约得体；运动→透气功能优先\n" +
            "【性别硬性规则】女性→可连衣裙、半身裙等女性款式；男性→禁裙装/女性化配饰\n" +
            "【城市气候】请结合%s的气候特点和季节规律，推荐适合当地实际穿着习惯的搭配。\n\n" +
            "=== 输出格式(严格JSON数组,3个元素) ===\n" +
            "[{\"scheme_name\":\"简约基础款\",\"style\":\"风格\",\"color\":\"主色系\"," +
            "\"top\":\"上装\",\"bottom\":\"下装\",\"shoes\":\"鞋子\",\"accessory\":\"配饰\"," +
            "\"match_reason\":\"搭配理由(结合身材肤色天气城市)\"," +
            "\"score_body\":0,\"score_weather\":0,\"score_occasion\":0,\"score_color\":0,\"score_total\":0," +
            "\"optimize_advice\":\"优化建议\",\"prompt\":\"英文AI绘图提示词\"}, " +
            "{\"scheme_name\":\"风格突出款\",...}, {\"scheme_name\":\"精致进阶款\",...}]\n" +
            "注意：3套方案必须差异化。评分每项0-100。",
            user.getGender(), user.getBodyType(), user.getSkinTone(),
            user.getStylePref(), user.getColorPref(), cityInfo, weather, temp,
            cityInfo
        );
    }

    private String generateFallback(User user, String input, String weather, String temp, String city) {
        String gender = user.getGender() != null ? user.getGender() : "女";
        String style = "通勤";
        if (user.getStylePref() != null && !user.getStylePref().isEmpty()) {
            style = user.getStylePref().split(",")[0];
        }
        String baseColor = "浅色系";
        if (user.getColorPref() != null && !user.getColorPref().isEmpty()) {
            baseColor = user.getColorPref().split(",")[0];
        }
        String cityName = (city != null && !city.isEmpty()) ? city : "北京";
        String[] styles = {style, style.equals("通勤")?"温柔":"休闲", style.equals("运动")?"活力":"甜美"};
        String[] names = {"简约基础款","风格突出款","精致进阶款"};
        List<Map<String,Object>> plans = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map<String,Object> p = new HashMap<>();
            p.put("scheme_name", names[i]);
            p.put("style", styles[i]);
            p.put("color", baseColor);
            if ("女".equals(gender)) {
                p.put("top", i==0?"白色修身衬衫":i==1?"蕾丝雪纺衫":"V领针织衫");
                p.put("bottom", i==0?"高腰直筒西裤":i==1?"A字半身裙":"阔腿休闲裤");
                p.put("shoes", i==0?"低跟尖头单鞋":"玛丽珍鞋");
                p.put("accessory", i==0?"简约手提包":"珍珠耳环");
            } else {
                p.put("top", i==0?"白色衬衫":i==1?"休闲Polo衫":"简约牛仔外套");
                p.put("bottom", i==0?"深色西裤":"卡其休闲裤");
                p.put("shoes", i==0?"商务皮鞋":"小白鞋");
                p.put("accessory","简约手表");
            }
            if (temp != null) {
                try {
                    int t = Integer.parseInt(temp);
                    if (t > 25) { p.put("top","纯棉短袖T恤"); p.put("bottom","A字短裤/短裤"); p.put("shoes","小白鞋"); }
                    else if (t < 15) { p.put("top","羊毛大衣/厚外套"); p.put("bottom","加绒牛仔裤"); p.put("shoes","短靴"); }
                } catch (Exception e) {}
            }
            p.put("match_reason", "这套"+styles[i]+"风格配色"+baseColor+"，结合您的"+user.getBodyType()+"身材和"+user.getSkinTone()+"肤色，突出优势修饰不足，适配"+weather+"天气及"+cityName+"当地气候。");
            p.put("score_body", 75 + (int)(Math.random()*20));
            p.put("score_weather", 80 + (int)(Math.random()*15));
            p.put("score_occasion", 75 + (int)(Math.random()*20));
            p.put("score_color", 70 + (int)(Math.random()*25));
            p.put("score_total", 75 + (int)(Math.random()*20));
            p.put("optimize_advice", "建议搭配简约配饰提升整体质感，注意色彩呼应。");
            String enGender = "女".equals(gender) ? "woman" : "man";
            p.put("prompt", "A full-body photo of a "+enGender+" wearing "+p.get("top")+", "+p.get("bottom")+", "+p.get("shoes")+", studio lighting, simple background, 8K, photorealistic, fashion photography.");
            plans.add(p);
        }
        return JSONUtil.toJsonStr(plans);
    }
}
