package com.outfit.dto;
import lombok.Data;
@Data
public class ForgotPasswordDTO {
    private String username;
    private String securityQuestion;
    private String securityAnswer;
    private String newPassword;
}