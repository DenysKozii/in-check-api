package com.incheck.api.service;

import com.incheck.api.dto.UserDto;

public interface UserService {

    UserDto info(String username) throws RuntimeException;

}
