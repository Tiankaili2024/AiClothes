package com.outfit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outfit.dto.LoginDTO;
import com.outfit.dto.RegisterDTO;
import com.outfit.dto.UserProfileDTO;
import com.outfit.entity.User;
import java.util.Map;

public interface UserService {
    Map<String, Object> login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getCurrentUser(Long userId);
    void updateProfile(Long userId, UserProfileDTO dto);
    void updatePassword(Long userId, String oldPwd, String newPwd);
    void updateAvatar(Long userId, String avatar);
    void updateFullBodyPhoto(Long userId, String fullBodyPhoto);
    Page<User> listUsers(int page, int size, String keyword);
    void toggleUserStatus(Long userId, Integer status);
    void forgotPassword(com.outfit.dto.ForgotPasswordDTO dto);
    void resetPassword(Long userId);
    String getSecurityQuestion(String username);
}
