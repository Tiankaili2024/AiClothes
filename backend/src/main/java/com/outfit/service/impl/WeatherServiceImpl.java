package com.outfit.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.outfit.mapper.SystemConfigMapper;
import com.outfit.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WeatherServiceImpl implements WeatherService {
    @Autowired private SystemConfigMapper configMapper;

    // 北方城市（秦岭-淮河以北，含东北、华北、西北）
    private static final Set<String> NORTH_CITIES = new HashSet<>(Arrays.asList(
        "北京","天津","石家庄","唐山","秦皇岛","邯郸","邢台","保定","张家口","承德","沧州","廊坊",
        "太原","大同","阳泉","长治","晋城","朔州","晋中","运城","忻州","临汾","吕梁",
        "呼和浩特","包头","乌海","赤峰","通辽","鄂尔多斯","呼伦贝尔","巴彦淖尔","乌兰察布",
        "沈阳","大连","鞍山","抚顺","本溪","丹东","锦州","营口","阜新","辽阳","盘锦","铁岭","朝阳","葫芦岛",
        "长春","吉林","四平","辽源","通化","白山","松原","白城","延边",
        "哈尔滨","齐齐哈尔","鸡西","鹤岗","双鸭山","大庆","伊春","佳木斯","七台河","牡丹江","黑河","绥化",
        "济南","青岛","淄博","枣庄","东营","烟台","潍坊","济宁","泰安","威海","日照","临沂","聊城","滨州","菏泽",
        "郑州","开封","洛阳","平顶山","安阳","鹤壁","新乡","焦作","濮阳","许昌","漯河","三门峡","商丘","周口","驻马店",
        "西安","铜川","宝鸡","咸阳","渭南","延安","汉中","榆林","安康","商洛",
        "兰州","嘉峪关","金昌","白银","天水","武威","张掖","平凉","酒泉","庆阳","定西","陇南",
        "西宁","海东",
        "银川","石嘴山","吴忠","固原","中卫",
        "乌鲁木齐","克拉玛依","吐鲁番","哈密",
        "拉萨","日喀则","昌都","林芝","山南","那曲",
        "徐州","连云港","淮安","盐城","宿迁","淮北","亳州","蚌埠","阜阳","淮南","宿州","滁州","六安"
    ));

    @Override
    public Map<String, Object> getWeather(String city) {
        String apiKey = configMapper.getValueByKey("weather_api_key");
        String apiUrl = configMapper.getValueByKey("weather_api_url");
        Map<String, Object> weather;
        if (apiKey == null || apiKey.isEmpty()) {
            weather = getDefaultWeather();
        } else {
            try {
                String resp = HttpUtil.get(String.format("%s?key=%s&city=%s&extensions=base", apiUrl, apiKey, city));
                JSONObject json = JSONUtil.parseObj(resp);
                if ("1".equals(json.getStr("status"))) {
                    JSONObject live = json.getJSONArray("lives").getJSONObject(0);
                    weather = new HashMap<>();
                    weather.put("city", live.getStr("city"));
                    weather.put("temperature", live.getStr("temperature"));
                    weather.put("weather", live.getStr("weather"));
                    weather.put("humidity", live.getStr("humidity"));
                    weather.put("windpower", live.getStr("windpower"));
                } else {
                    weather = getDefaultWeather();
                }
            } catch (Exception ignored) {
                weather = getDefaultWeather();
            }
        }
        // 补充季节和南北判定
        int temp = Integer.parseInt((String) weather.get("temperature"));
        weather.put("season", getSeason(temp));
        weather.put("regionType", isNorthCity((String) weather.get("city")) ? "北方" : "南方");
        return weather;
    }

    @Override
    public boolean isNorthCity(String city) {
        if (city == null || city.isEmpty()) return true;
        // 前缀匹配支持"北京"匹配"北京市"、"北京朝阳"等
        for (String nc : NORTH_CITIES) {
            if (city.contains(nc)) return true;
        }
        return false;
    }

    @Override
    public String getSeason(int temp) {
        if (temp >= 28) return "夏季";
        if (temp >= 20) return "春末夏初";
        if (temp >= 15) return "春季";
        if (temp >= 5)  return "秋季";
        return "冬季";
    }

    @Override
    public Map<String, Object> getDefaultWeather() {
        Map<String, Object> result = new HashMap<>();
        result.put("city", "默认城市");
        result.put("temperature", "25");
        result.put("weather", "晴");
        result.put("season", "春季");
        return result;
    }
}