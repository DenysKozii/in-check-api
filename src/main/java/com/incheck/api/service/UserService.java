package com.incheck.api.service;

import com.incheck.api.dto.UserStatsResponseDto;

public interface UserService {

    String getId(String username) throws RuntimeException;

    UserStatsResponseDto getStats(String username);
}
