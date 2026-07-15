package com.outfit.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.outfit.dto.LoginDTO;
import com.outfit.dto.RegisterDTO;
import com.outfit.dto.UserProfileDTO;
import com.outfit.entity.User;
import com.outfit.mapper.UserMapper;
import com.outfit.service.UserService;
import com.outfit.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired private UserMapper userMapper;
    @Autowired private JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        String encryptedPwd = DigestUtil.md5Hex(dto.getPassword());
        if (!user.getPassword().equals(encryptedPwd)) {
            throw new RuntimeException("密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public void register(RegisterDTO dto) {
        User exist = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtil.md5Hex(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateProfile(Long userId, UserProfileDTO dto) {
        User user = new User();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setHeight(dto.getHeight());
        user.setWeight(dto.getWeight());
        user.setBodyType(dto.getBodyType());
        user.setSkinTone(dto.getSkinTone());
        user.setStylePref(dto.getStylePref());
        user.setColorPref(dto.getColorPref());
        user.setCity(dto.getCity());
        user.setFashionBan(dto.getFashionBan());
        user.setSecurityQuestion(dto.getSecurityQuestion());
        user.setSecurityAnswer(dto.getSecurityAnswer());
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPwd, String newPwd) {
        User user = userMapper.selectById(userId);
        if (!user.getPassword().equals(DigestUtil.md5Hex(oldPwd))) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(DigestUtil.md5Hex(newPwd));
        userMapper.updateById(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        User user = new User();
        user.setId(userId);
        user.setAvatar(avatar);
        userMapper.updateById(user);
    }

    @Override
    public Page<User> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void forgotPassword(com.outfit.dto.ForgotPasswordDTO dto) {
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if(user == null) throw new RuntimeException("用户不存在");
        if(!dto.getSecurityAnswer().equals(user.getSecurityAnswer())) throw new RuntimeException("密保答案错误");
        user.setPassword(DigestUtil.md5Hex(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setPassword(DigestUtil.md5Hex("123456"));
        userMapper.updateById(user);
    }

    @Override
    public String getSecurityQuestion(String username) {
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if(user == null) return "";
        return user.getSecurityQuestion() != null ? user.getSecurityQuestion() : "";
    }

    @Override
    public void toggleUserStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        userMapper.updateById(user);
    }
}
