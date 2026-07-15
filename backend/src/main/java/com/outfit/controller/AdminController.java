package com.outfit.controller;
import com.outfit.common.Result;
import com.outfit.entity.OperationLog;
import com.outfit.entity.SystemConfig;
import com.outfit.mapper.OperationLogMapper;
import com.outfit.mapper.SystemConfigMapper;
import com.outfit.service.DashboardService;
import com.outfit.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired private UserService userService;
    @Autowired private DashboardService dashboardService;
    @Autowired private SystemConfigMapper configMapper;
    @Autowired private OperationLogMapper logMapper;

    private Result<?> checkAdmin(HttpServletRequest r) {
        Integer role = (Integer) r.getAttribute("role");
        if (role == null || role != 1) return Result.error(403, "???????????");
        return null;
    }

    @GetMapping("/dashboard")
    public Result<?> dashboard(HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        return Result.success(dashboardService.getDashboardData());
    }

    @GetMapping("/users")
    public Result<?> listUsers(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="10") int size,
                               @RequestParam(required=false) String keyword, HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        return Result.success(userService.listUsers(page, size, keyword));
    }

    @PutMapping("/user/status/{userId}")
    public Result<?> toggleStatus(@PathVariable Long userId, @RequestBody com.outfit.dto.UserStatusDTO dto, HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        userService.toggleUserStatus(userId, dto.getStatus()); return Result.success();
    }

    @PutMapping("/user/reset-password/{userId}")
    public Result<?> resetPassword(@PathVariable Long userId, HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        userService.resetPassword(userId); return Result.success("??????123456");
    }

    @GetMapping("/config")
    public Result<?> getConfig(HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        return Result.success(configMapper.selectList(null));
    }

    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody List<SystemConfig> configs, HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        for (SystemConfig c : configs) {
            LambdaUpdateWrapper<SystemConfig> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SystemConfig::getId, c.getId());
            wrapper.set(SystemConfig::getConfigValue, c.getConfigValue());
            configMapper.update(null, wrapper);
        }
        return Result.success();
    }

    @GetMapping("/logs")
    public Result<?> getLogs(@RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size,
                             @RequestParam(required=false) Integer logType, HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreatedAt);
        if (logType != null) wrapper.eq(OperationLog::getLogType, logType);
        return Result.success(logMapper.selectPage(new Page<>(page, size), wrapper));
    }

    @GetMapping("/portrait")
    public Result<?> globalPortrait(HttpServletRequest r) {
        Result<?> err = checkAdmin(r); if (err != null) return err;
        return Result.success(dashboardService.getGlobalPortrait());
    }
}
