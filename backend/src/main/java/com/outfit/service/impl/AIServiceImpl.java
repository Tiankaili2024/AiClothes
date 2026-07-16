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

    public String generateOutfitPlan(User user, String userInput, String weather, String temp, String city, String season, String regionType, String sessionContext) {
        String apiKey = configMapper.getValueByKey("ai_model_api_key");
        String apiUrl = configMapper.getValueByKey("ai_model_api_url");
        String systemPrompt = buildSystemPrompt(user, weather, temp, city, season, regionType);
        if (sessionContext != null && !sessionContext.isEmpty()) {
            systemPrompt += "\n历史对话上下文：" + sessionContext;
        }
        String userPrompt = "用户需求：" + userInput;
        if (user.getFashionBan() != null && !user.getFashionBan().isEmpty()) {
            userPrompt += "\n穿搭禁忌（请严格遵守）：" + user.getFashionBan();
        }
        userPrompt += "\n请严格按照上述JSON格式输出3套穿搭方案（对应三个场景），用数组包裹。";

        if (apiKey != null && !apiKey.isEmpty()) {
            try {
                return callCoze(apiUrl, apiKey, systemPrompt, userPrompt);
            } catch (Exception e) {
                return generateFallback(user, userInput, weather, temp, city, season, regionType);
            }
        }
        return generateFallback(user, userInput, weather, temp, city, season, regionType);
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

    private String buildSystemPrompt(User user, String weather, String temp, String city, String season, String regionType) {
        String cityInfo = (city != null && !city.isEmpty()) ? city : "未知城市";
        String seasonInfo = (season != null && !season.isEmpty()) ? season : "春季";
        String regionInfo = (regionType != null && !regionType.isEmpty()) ? regionType : "北方";
        String regionRule = buildRegionRule(regionInfo, seasonInfo);

        return String.format(
            "你是一位资深专业AI穿搭搭配师。请严格按照以下规则为用户生成3套差异化穿搭方案，分别对应三个场景。\n\n" +

            "=== 用户档案 ===\n" +
            "性别：%s | 身材：%s | 肤色：%s\n" +
            "风格偏好：%s | 色系偏好：%s\n" +
            "常驻城市：%s | 所属区域：%s | 当前季节：%s\n" +
            "今日天气：%s | 温度：%s℃\n\n" +

            "=== 四季服饰参考清单 ===\n" +
            "【春季】薄卫衣、针织开衫、薄夹克、风衣、牛仔外套、长袖衬衫、薄长裤、直筒牛仔裤、帆布鞋、小白鞋\n" +
            "【夏季】短袖T恤、吊带、背心、短裤、半身短裙、A字裙、冰丝阔腿裤、防晒衣、凉鞋、拖鞋、渔夫帽\n" +
            "【秋季】风衣、长袖衬衫、薄毛衣、针织衫、休闲西裤、灯芯绒裤、马丁靴、切尔西靴\n" +
            "【冬季】羽绒服、棉服、毛呢大衣、加绒卫衣、厚毛衣、加绒裤、围巾、手套、雪地靴、短靴\n\n" +

            "=== 南北方气候差异规则 ===\n%s\n\n" +

            "=== 三大穿搭场景（必须各输出一套） ===\n" +
            "【场景1：日常通勤】得体大方、简约专业，适合办公室/学校，注意层次感和色彩协调\n" +
            "【场景2：户外休闲】舒适自在、活动方便，适合逛街/出游/运动，优先功能性单品\n" +
            "【场景3：约会出行】精致有亮点、温柔显气质，注重细节搭配，适当运用流行元素\n\n" +

            "=== 专业穿搭规则 ===\n" +
            "【身材适配】梨形→上紧下松避雷紧身下装；小个子→高腰款式优化比例；微胖→H版型遮肉；苹果型→修饰腰线\n" +
            "【肤色适配】黄黑皮→避雷荧光色/高饱和冷色，推荐暖调低饱和色系；冷白皮→适配范围广\n" +
            "【温度适配】<5℃→羽绒服+加绒裤+雪地靴；5-15℃→毛呢大衣/棉服+毛衣+长裤；15-25℃→薄外套+长袖/衬衫+长裤；>25℃→短袖/吊带+短裤/裙子+凉鞋\n" +
            "【雨天规则】遇雨必推防水外套或带帽款，鞋子优选防滑材质\n" +
            "【性别硬性规则】女性→可连衣裙、半身裙等女性款式；男性→禁裙装/女性化配饰\n\n" +

            "=== 输出格式(严格JSON数组,3个元素,对应3个场景) ===\n" +
            "[{\"scene\":\"日常通勤\",\"scheme_name\":\"方案名称\",\"style\":\"风格标签\",\"color\":\"主色系\"," +
            "\"top\":\"上装单品\",\"bottom\":\"下装单品\",\"shoes\":\"鞋子\",\"accessory\":\"配饰\"," +
            "\"temp_note\":\"适配气温说明(如：适合15-22℃穿着，早晚微凉备薄外套)\"," +
            "\"match_reason\":\"搭配理由(结合身材/肤色/天气/城市气候)\"," +
            "\"score_body\":0,\"score_weather\":0,\"score_occasion\":0,\"score_color\":0,\"score_total\":0," +
            "\"optimize_advice\":\"优化建议\"," +
            "\"prompt\":\"英文AI绘图提示词\"}, " +
            "{\"scene\":\"户外休闲\",...}, " +
            "{\"scene\":\"约会出行\",...}]\n\n" +

            "注意：3套必须分别对应3个场景，差异明显。temp_note要结合所在城市%s的%s气候和%s特征。评分每项0-100。",
            // 用户档案参数
            user.getGender(), user.getBodyType(), user.getSkinTone(),
            user.getStylePref(), user.getColorPref(),
            cityInfo, regionInfo, seasonInfo, weather, temp,
            // 南北规则
            regionRule,
            // 城市气候
            cityInfo, regionInfo, seasonInfo
        );
    }

    private String buildRegionRule(String regionType, String season) {
        if ("北方".equals(regionType)) {
            if ("冬季".equals(season)) {
                return "【北方冬季严寒】主推长款厚羽绒服/毛呢大衣，内搭厚毛衣+加绒裤+雪地靴，围巾手套必备。叠穿保暖优先，室外零下需防风面料。";
            } else if (season.contains("春") || season.contains("秋")) {
                return "【北方春秋】昼夜温差大（可达10-15℃），建议叠穿法：内搭薄长袖+中层开衫/卫衣+外层薄夹克/风衣，方便随时增减。下装以牛仔裤/休闲长裤为主。";
            } else {
                return "【北方夏季】白天炎热但早晚微凉，推荐短袖+薄长裤/长裙，随身备轻薄防晒衣应对早晚温差和空调房。";
            }
        } else {
            if ("冬季".equals(season)) {
                return "【南方冬季湿冷】无需厚重长款羽绒服，推荐短款棉服/轻薄羽绒服+毛衣+加绒裤即可。重点防风防潮，面料选抗湿冷材质。";
            } else if (season.contains("春") || season.contains("秋")) {
                return "【南方春秋】气温温和，可单穿长袖衬衫/薄卫衣+休闲长裤/半身裙，无需厚重外套。";
            } else {
                return "【南方夏季闷热】优先透气轻薄单品：冰丝面料、棉麻材质、速干T恤。短袖+短裤/短裙+凉鞋为主，防晒衣必备。避免深色吸热。";
            }
        }
    }

    private String generateFallback(User user, String input, String weather, String temp, String city, String season, String regionType) {
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
        String region = (regionType != null && !regionType.isEmpty()) ? regionType : "北方";
        String seasonName = (season != null && !season.isEmpty()) ? season : "春季";

        String[][] scenes = {
            {"日常通勤","简约通勤风","得体大方适合职场"},
            {"户外休闲","轻松休闲风","舒适自在方便活动"},
            {"约会出行","精致约会风","温柔显气质有亮点"}
        };

        List<Map<String,Object>> plans = new ArrayList<>();
        int t = 25;
        try { t = Integer.parseInt(temp); } catch (Exception e) {}

        for (int i = 0; i < 3; i++) {
            Map<String,Object> p = new HashMap<>();
            p.put("scene", scenes[i][0]);
            p.put("scheme_name", scenes[i][0] + "穿搭");
            p.put("style", scenes[i][1]);
            p.put("color", baseColor);

            if ("女".equals(gender)) {
                if (t < 5) {
                    p.put("top", i==0?"长款羽绒服+高领毛衣":i==1?"毛呢大衣+针织衫":"短款羽绒服+连衣裙");
                    p.put("bottom", i==0?"加绒直筒西裤":"加绒牛仔裤");
                    p.put("shoes", i==0?"方跟短靴":"雪地靴");
                    p.put("accessory", "羊绒围巾+皮手套");
                    p.put("temp_note", "适合0-5℃寒冷天气，" + region + "冬季注意防风保暖");
                } else if (t < 15) {
                    p.put("top", i==0?"风衣+衬衫":i==1?"卫衣+牛仔外套":"针织开衫+打底衫");
                    p.put("bottom", i==0?"直筒西裤":"A字半身裙");
                    p.put("shoes", i==0?"乐福鞋":"马丁靴");
                    p.put("accessory", i==0?"简约手提包":"贝雷帽");
                    p.put("temp_note", "适合" + t + "℃，" + (region.equals("北方")?"早晚温差大建议叠穿":"单穿舒适"));
                } else if (t < 25) {
                    p.put("top", i==0?"长袖衬衫":i==1?"薄针织衫":"条纹T恤+薄开衫");
                    p.put("bottom", i==0?"九分西裤":"A字半身短裙");
                    p.put("shoes", i==0?"尖头平底鞋":"帆布鞋");
                    p.put("accessory", i==0?"简约链包":"棒球帽");
                    p.put("temp_note", "适合" + t + "℃温暖天气，早晚微凉备薄外套");
                } else {
                    p.put("top", i==0?"纯棉短袖衬衫":i==1?"冰丝T恤":"吊带+防晒开衫");
                    p.put("bottom", i==0?"冰丝阔腿裤":"A字短裙");
                    p.put("shoes", i==0?"凉鞋":"小白鞋");
                    p.put("accessory", "渔夫帽+墨镜");
                    p.put("temp_note", "适合" + t + "℃炎热天气，" + region + "夏季优先透气轻薄面料");
                }
            } else {
                if (t < 5) {
                    p.put("top", i==0?"长款羽绒服+厚毛衣":"棉服+加绒卫衣");
                    p.put("bottom", "加绒休闲裤");
                    p.put("shoes", i==0?"商务短靴":"加绒运动鞋");
                    p.put("accessory", "围巾+手套");
                    p.put("temp_note", "适合0-5℃寒冷天气，" + region + "冬季注意防风保暖");
                } else if (t < 15) {
                    p.put("top", i==0?"风衣+衬衫":i==1?"夹克+长袖T":"针织衫+衬衫");
                    p.put("bottom", i==0?"休闲西裤":"直筒牛仔裤");
                    p.put("shoes", i==0?"商务皮鞋":"小白鞋");
                    p.put("accessory", "简约手表");
                    p.put("temp_note", "适合" + t + "℃，" + (region.equals("北方")?"早晚温差大建议叠穿":"单穿舒适"));
                } else if (t < 25) {
                    p.put("top", i==0?"长袖衬衫":i==1?"薄卫衣":"Polo衫");
                    p.put("bottom", i==0?"卡其休闲裤":"深色牛仔裤");
                    p.put("shoes", i==0?"乐福鞋":"小白鞋");
                    p.put("accessory", "简约手表");
                    p.put("temp_note", "适合" + t + "℃温暖舒适天气");
                } else {
                    p.put("top", i==0?"纯棉短袖衬衫":i==1?"速干T恤":"宽松Polo衫");
                    p.put("bottom", i==0?"冰丝休闲裤":"棉质短裤");
                    p.put("shoes", i==0?"透气运动鞋":"凉鞋");
                    p.put("accessory", "棒球帽+太阳镜");
                    p.put("temp_note", "适合" + t + "℃炎热天气，" + region + "夏季优先透气轻薄面料");
                }
            }
            p.put("match_reason", scenes[i][2] + "，结合您" + user.getBodyType() + "身材与" + user.getSkinTone() + "肤色，" + cityName + "(" + region + ")" + seasonName + "气候，适配" + weather + "天气" + t + "℃。");
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