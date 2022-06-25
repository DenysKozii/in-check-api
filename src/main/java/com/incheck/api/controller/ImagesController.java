package com.incheck.api.controller;

import com.incheck.api.service.ImageService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/image")
public class ImagesController {

    private final ImageService imageService;

    @GetMapping("{filename}")
    public String getImage(@PathVariable String filename) {
        return imageService.readeImage(filename);
    }

}
