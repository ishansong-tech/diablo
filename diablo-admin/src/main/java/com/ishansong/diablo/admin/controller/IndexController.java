package com.ishansong.diablo.admin.controller;

import com.ishansong.diablo.admin.listener.DiabloDomain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String index(final Model model) {
        model.addAttribute("domain", DiabloDomain.getInstance().getHttpPath());
        return "index";
    }

}
