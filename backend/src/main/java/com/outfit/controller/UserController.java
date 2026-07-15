package com.outfit.controller;
import com.outfit.common.Result;
import com.outfit.dto.UserProfileDTO;
import com.outfit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired private UserService userService;
    @GetMapping("/info")
    public Result<?> getInfo(HttpServletRequest r) { return Result.success(userService.getCurrentUser((Long)r.getAttribute("userId"))); }
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UserProfileDTO dto, HttpServletRequest r) {
        try { userService.updateProfile((Long)r.getAttribute("userId"), dto); return Result.success();
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String,String> p, HttpServletRequest r) {
        try { userService.updatePassword((Long)r.getAttribute("userId"), p.get("oldPassword"), p.get("newPassword")); return Result.success();
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @PutMapping("/avatar")
    public Result<?> updateAvatar(@RequestBody Map<String,String> p, HttpServletRequest r) {
        userService.updateAvatar((Long)r.getAttribute("userId"), p.get("avatar")); return Result.success();
    }
    @GetMapping("/security-question")
    public Result<?> getSecurityQuestion(@RequestParam String username) {
        return Result.success(userService.getSecurityQuestion(username));
    }
}
