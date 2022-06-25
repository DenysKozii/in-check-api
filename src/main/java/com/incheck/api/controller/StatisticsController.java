package com.incheck.api.controller;

import com.incheck.api.dto.UserDto;
import com.incheck.api.service.StatisticsService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("{username}")
    public UserDto getUserInfo(@PathVariable String username) throws RuntimeException {
        return statisticsService.getUserInfo(username);
    }

}
