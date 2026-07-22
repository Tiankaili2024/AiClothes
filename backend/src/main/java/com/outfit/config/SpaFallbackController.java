package com.outfit.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaFallbackController {
    @RequestMapping(value = {"/login", "/register", "/forgot-password", "/outfit", "/profile", "/portrait", "/history", "/favorite", "/admin/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
