package com.incheck.api.controller;

import com.incheck.api.enums.TagInfo;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/image")
public class ImagesController {

    @GetMapping
    public String getImage(@RequestParam TagInfo tagInfo) {
        return tagInfo.getTitle();
    }

}
