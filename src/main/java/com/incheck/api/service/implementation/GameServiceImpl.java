package com.incheck.api.service.implementation;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.ListGameResponses;
import com.incheck.api.dto.MoveDto;
import com.incheck.api.service.GameService;
import com.incheck.api.service.UserService;
import com.incheck.api.utils.AbstractHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GameServiceImpl extends AbstractHttpClient implements GameService {

    @Autowired
    private UserService userService;

    @Value("${chess-api-games-url}")
    private              String GAMES_URL;
    @Value("${chess-api-game-info-url}")
    private              String GAME_INFO_URL;
    private final static String MOVE_REGEX  = "(\\.\\\\u0020.\\\\u0020)|(\\.\\\\u0020..\\\\u0020)|(\\.\\\\u0020...\\\\u0020)|(\\.\\\\u0020....\\\\u0020)|(\\.\\\\u0020.....\\\\u0020)";
    private final static String PGN_REGEX   = "(pgn:).*";
    private final static String COLOR_REGEX = "(White\\\\u0020\\\\u0022.+?(?=\\\\))|(Black\\\\u0020\\\\u0022.+?(?=\\\\))";

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
    public List<MoveDto> gameMoves(String gameId, String username) {
        try {
            Document document = Jsoup.connect(GAME_INFO_URL + gameId).get();
            Elements html = document.getAllElements();
            Pattern pattern = Pattern.compile(PGN_REGEX);
            Matcher matcher = pattern.matcher(html.toString());
            String pgn = "";
            String opponentUsername = "";
            boolean white = false;
            if (matcher.find()) {
                pgn = matcher.group();
            }
            pattern = Pattern.compile(COLOR_REGEX);
            matcher = pattern.matcher(html.toString());
            if (matcher.find()) {
                String whiteUsername = matcher.group();
                white = whiteUsername.contains(username);
                if (matcher.find()) {
                    if (white) {
                        opponentUsername = matcher.group().replace("Black\\u0020\\u0022", "");
                    } else {
                        opponentUsername = whiteUsername.replace("White\\u0020\\u0022", "");
                    }
                }
            }
            pattern = Pattern.compile(MOVE_REGEX);
            matcher = pattern.matcher(pgn);
            List<MoveDto> moves = new ArrayList<>();
            int moveCounter = 0;
            while (matcher.find()) {
                String move = matcher.group()
                                     .replace("\\u0020", "")
                                     .replace(".", "");
                MoveDto moveDto = new MoveDto();
                moveDto.setMove(move);
                if (white) {
                    if (moveCounter % 2 == 0) {
                        moveDto.setUsername(username);
                    } else {
                        moveDto.setUsername(opponentUsername);
                    }
                } else {
                    if (moveCounter % 2 == 0) {
                        moveDto.setUsername(opponentUsername);
                    } else {
                        moveDto.setUsername(username);
                    }
                }
                moveCounter++;
                moves.add(moveDto);
            }
            return moves;
        } catch (IOException e) {
            log.error("Parse html has an error by url: {}", GAME_INFO_URL + gameId);
            throw new RuntimeException(String.format("Jsoup connection failed for url: %s", GAME_INFO_URL + gameId));
        }
    }

    @Override
    public List<List<MoveDto>> getAllMoves(String username) {
        String userId = userService.getId(username);
        List<GameDto> games = gamesByUserId(userId);
        return games.stream().map(g -> gameMoves(g.getId(), username)).collect(Collectors.toList());
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}
