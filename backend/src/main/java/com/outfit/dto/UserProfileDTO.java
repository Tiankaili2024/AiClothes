package com.outfit.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String nickname;
    private String avatar; private String fullBodyPhoto;
    private String gender;
    private String height;
    private String weight;
    private String bodyType;
    private String skinTone;
    private String stylePref;
    private String colorPref;
    private String city;
    private String fashionBan;
    private String securityQuestion;
    private String securityAnswer;
}
