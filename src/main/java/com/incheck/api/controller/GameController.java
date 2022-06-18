package com.incheck.api.controller;

import com.incheck.api.dto.GameDto;
import com.incheck.api.service.GameService;
import com.incheck.api.service.UserService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/game")
public class GameController {

    private final GameService gameService;

    @GetMapping("{userId}")
    public List<GameDto> allGames(@PathVariable String userId) {
       return gameService.gamesByUserId(userId);
    }

}
