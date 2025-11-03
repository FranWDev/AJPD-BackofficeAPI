package org.dubini.backofficeAPI.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/editor")
public class EditorController {

    @GetMapping
    public String editor() {
        return "editor";
    }

    @GetMapping("/noticias-y-actividades")
    public String news() {
        return "news";
    }

}