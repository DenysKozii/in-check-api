package com.incheck.api.service.implementation;

import com.incheck.api.service.ImageService;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    @Value("${images-directory}")
    private String DIRECTORY;

    @Override
    public String readImage(String filename) {
        String path = DIRECTORY + filename;
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
