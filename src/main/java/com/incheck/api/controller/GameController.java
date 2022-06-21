package com.incheck.api.controller;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.UserDto;
import com.incheck.api.service.GameService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("api/v1/game")
public class GameController {

    private final GameService gameService;

    @GetMapping("{username}")
    public List<GameDto> allMoves(@PathVariable String username) {
        return gameService.getAllMoves(username);
    }

    @GetMapping("statistics/{username}")
    public UserDto getStatistics(@PathVariable String username) throws RuntimeException {
        return gameService.getStatistics(username);
    }

}
