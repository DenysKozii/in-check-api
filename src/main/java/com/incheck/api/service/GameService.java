package com.incheck.api.service;

import com.incheck.api.dto.GameDto;

import java.util.List;

public interface GameService {

    List<GameDto> gamesByUserId(String userId) throws RuntimeException;

    List<String> gameMoves(String gameId);

    List<List<String>> getAllMoves(String username);
}
