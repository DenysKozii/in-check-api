package com.incheck.api.service.implementation;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.ListGameResponses;
import com.incheck.api.service.GameService;
import com.incheck.api.utils.AbstractHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GameServiceImpl extends AbstractHttpClient implements GameService {

    @Value("${chess-api-games-url}")
    private String GAMES_URL;

    public GameServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<GameDto> gamesByUserId(String userId) throws RuntimeException {
        try {
            return get(GAMES_URL + userId, ListGameResponses.class);
        }catch (RuntimeException e) {
            log.error("error while getting games by url {}", GAMES_URL);
        }
        return Collections.emptyList();
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}
