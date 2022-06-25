package com.incheck.api.service;

import com.incheck.api.dto.GamesResponseDto;
import com.incheck.api.dto.UserDto;
import com.incheck.api.dto.UserStatsResponseDto;

public interface StatisticsService {

    GamesResponseDto getAllGames(String username);

    UserDto getUserInfo(String username);

    UserStatsResponseDto getStats(String username);

}
