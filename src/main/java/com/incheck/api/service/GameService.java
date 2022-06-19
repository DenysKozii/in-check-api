package com.incheck.api.service;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.MoveDto;

import java.util.List;

public interface GameService {

    List<GameDto> gamesByUserId(String userId) throws RuntimeException;

    List<MoveDto> gameMoves(String gameId, String username);

    List<List<MoveDto>> getAllMoves(String username);
}
