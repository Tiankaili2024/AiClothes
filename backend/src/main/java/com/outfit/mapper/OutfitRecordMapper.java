package com.outfit.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outfit.entity.OutfitRecord;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;
@Mapper
public interface OutfitRecordMapper extends BaseMapper<OutfitRecord> {
    @Select("SELECT style_tags,COUNT(*) as count FROM outfit_record WHERE style_tags IS NOT NULL GROUP BY style_tags ORDER BY count DESC")
    List<Map<String,Object>> countByStyle();
    @Select("SELECT HOUR(created_at) as hour,COUNT(*) as count FROM outfit_record GROUP BY HOUR(created_at) ORDER BY hour")
    List<Map<String,Object>> countByHour();
    @Select("SELECT style_tags,COUNT(*) as count FROM outfit_record WHERE user_id=#{uid} AND status=1 AND style_tags IS NOT NULL GROUP BY style_tags ORDER BY count DESC")
    List<Map<String,Object>> userStyleStats(@Param("uid") Long uid);
    @Select("SELECT COUNT(*) as cnt,COALESCE(SUM(score_total),0) as total FROM outfit_record WHERE user_id=#{uid} AND status=1")
    Map<String,Object> userScoreStats(@Param("uid") Long uid);
}