package com.outfit.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outfit.entity.ConversationSession;
import org.apache.ibatis.annotations.*;
@Mapper
public interface ConversationSessionMapper extends BaseMapper<ConversationSession> {
    @Select("SELECT * FROM conversation_session WHERE session_id=#{s} AND user_id=#{uid}")
    ConversationSession getBySession(@Param("s") String s, @Param("uid") Long uid);
}