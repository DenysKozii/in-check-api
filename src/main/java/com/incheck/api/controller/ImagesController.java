package com.incheck.api.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/image")
public class ImagesController {

    private static final String DIRECTORY = "src/main/resources/images";

    @GetMapping
    public String getImage() {
        return readeImage("base-position");
    }

    private String readeImage(String filename) {
        String path = DIRECTORY + "/" + filename;
        byte[] fileContent = new byte[0];
        try {
            fileContent = FileUtils.readFileToByteArray(new File(path + ".jpg"));
        } catch (IOException e) {
            try {
                fileContent = FileUtils.readFileToByteArray(new File(path + ".png"));
            } catch (IOException ex) {
                try {
                    fileContent = FileUtils.readFileToByteArray(new File(path + ".jpeg"));
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        }
        return Base64.getEncoder().encodeToString(fileContent);
    }

}
