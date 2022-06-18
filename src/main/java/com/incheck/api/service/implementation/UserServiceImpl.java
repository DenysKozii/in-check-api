package com.incheck.api.service.implementation;

import com.incheck.api.dto.UserDto;
import com.incheck.api.service.UserService;
import com.incheck.api.utils.AbstractHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl extends AbstractHttpClient implements UserService {

    @Value("${chess-api-stats-url}")
    private String STATS_URL;

    public UserServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public UserDto info(String username) throws RuntimeException {
        try {
            return get(STATS_URL + username, UserDto.class);
        }catch (RuntimeException e) {
            log.error("error while getting category by url {}", STATS_URL);
        }
        return new UserDto();
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }
}
