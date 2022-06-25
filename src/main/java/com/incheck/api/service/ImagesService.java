package com.incheck.api.service;

import com.incheck.api.dto.UserStatsResponseDto;

public interface ImagesService {

    UserStatsResponseDto getStats(String username);

}
