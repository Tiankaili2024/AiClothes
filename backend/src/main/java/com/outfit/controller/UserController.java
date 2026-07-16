package com.outfit.controller;

import com.outfit.common.Result;
import com.outfit.dto.UserProfileDTO;
import com.outfit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired private UserService userService;

    @Value("${app.upload-dir:uploads/avatar}")
    private String uploadDir;

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

    @PostMapping("/avatar/upload")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest r) throws IOException {
        String dir = System.getProperty("user.dir") + File.separator + uploadDir;
        File dirFile = new File(dir);
        if (!dirFile.exists()) dirFile.mkdirs();
        String ext = "";
        String origName = file.getOriginalFilename();
        if (origName != null && origName.contains(".")) {
            ext = origName.substring(origName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + ext;
        File dest = new File(dirFile, fileName);
        file.transferTo(dest);
        String avatarUrl = "/uploads/avatar/" + fileName;
        userService.updateAvatar((Long)r.getAttribute("userId"), avatarUrl);
        return Result.success(avatarUrl);
    }

    @PostMapping("/fullbody/upload")
    public Result<?> uploadFullBody(@RequestParam("file") MultipartFile file, HttpServletRequest r) throws IOException {
        String dir = System.getProperty("user.dir") + File.separator + uploadDir;
        File dirFile = new File(dir);
        if (!dirFile.exists()) dirFile.mkdirs();
        String ext = "";
        String origName = file.getOriginalFilename();
        if (origName != null && origName.contains(".")) {
            ext = origName.substring(origName.lastIndexOf("."));
        }
        String fileName = "fullbody_" + UUID.randomUUID().toString() + ext;
        File dest = new File(dirFile, fileName);
        file.transferTo(dest);
        String photoUrl = "/uploads/avatar/" + fileName;
        userService.updateFullBodyPhoto((Long)r.getAttribute("userId"), photoUrl);
        return Result.success(photoUrl);
    }

    @GetMapping("/security-question")
    public Result<?> getSecurityQuestion(@RequestParam String username) {
        return Result.success(userService.getSecurityQuestion(username));
    }
}