package com.incheck.api.controller;

import com.incheck.api.dto.UserDto;
import com.incheck.api.service.UserService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    @GetMapping("{username}")
    public String info(@PathVariable String username) throws RuntimeException {
        return userService.info(username);
    }

}
