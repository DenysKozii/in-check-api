package com.incheck.api.service;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.GameIdResponseDto;
import com.incheck.api.dto.MoveDto;
import com.incheck.api.dto.UserDto;

import java.util.List;

public interface GameService {

    List<GameIdResponseDto> gamesByUserId(String userId) throws RuntimeException;

    GameDto gameMoves(String gameId, String username);

    List<GameDto> getAllMoves(String username);

    String getOpenings(String username) throws RuntimeException;

    UserDto getStatistics(String username);
}
