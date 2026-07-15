package com.outfit.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.outfit.entity.SystemConfig;
import org.apache.ibatis.annotations.*;
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    @Select("SELECT config_value FROM sys_config WHERE config_key=#{k}")
    String getValueByKey(@Param("k") String k);
}