package com.incheck.api.service;

import com.incheck.api.dto.GamesResponseDto;
import com.incheck.api.dto.UserDto;

public interface GameService {

    GamesResponseDto getAllGames(String username);

    UserDto getStatistics(String username);

}
