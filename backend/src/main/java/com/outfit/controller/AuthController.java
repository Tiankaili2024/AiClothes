package com.outfit.controller;
import com.outfit.common.Result;
import com.outfit.dto.LoginDTO;
import com.outfit.dto.RegisterDTO;
import com.outfit.dto.ForgotPasswordDTO;
import com.outfit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired private UserService userService;
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        try { return Result.success(userService.login(dto));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        try { userService.register(dto); return Result.success();
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
    @PostMapping("/forgot-password")
    public Result<?> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        try { userService.forgotPassword(dto); return Result.success();
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }
}
