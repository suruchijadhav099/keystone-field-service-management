package com.zidio.keystone.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/", "/login"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}