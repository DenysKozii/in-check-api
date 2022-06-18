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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

@Service
@Slf4j
public class GameServiceImpl extends AbstractHttpClient implements GameService {

    @Value("${chess-api-games-url}")
    private String GAMES_URL;
    @Value("${chess-api-game-info-url}")
    private String GAME_INFO_URL;
    private final static String MOVE_REGEX = "(\\.\\\\u0020.\\\\u0020)|(\\.\\\\u0020..\\\\u0020)|(\\.\\\\u0020...\\\\u0020)|(\\.\\\\u0020....\\\\u0020)|(\\.\\\\u0020.....\\\\u0020)";
    private final static String PGN_REGEX = "(pgn:).*";

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
    public List<String> gameMoves(String userId, Integer id) {
        GameDto gameDto = gamesByUserId(userId).get(id);
        try {
            Document document = Jsoup.connect(GAME_INFO_URL + gameDto.getId()).get();
            Elements html = document.getAllElements();
            Pattern pattern = Pattern.compile(PGN_REGEX);
            Matcher matcher = pattern.matcher(html.toString());
            String pgn = "";
            if (matcher.find()) {
                pgn = matcher.group();
            }
            pattern = Pattern.compile(MOVE_REGEX);
            matcher = pattern.matcher(pgn);
            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group());
            }
            return matches.stream()
                          .map(s->s.replace("\\u0020", "")
                                   .replace(".", ""))
                          .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Parse html has an error by url: {}", GAME_INFO_URL + gameDto.getId());
            throw new RuntimeException(String.format("Jsoup connection failed for url: %s", GAME_INFO_URL + gameDto.getId()));
        }
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}
