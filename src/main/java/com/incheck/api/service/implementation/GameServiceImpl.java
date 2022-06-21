package com.incheck.api.service.implementation;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.GameIdResponseDto;
import com.incheck.api.dto.ListGameIdResponses;
import com.incheck.api.dto.MoveDto;
import com.incheck.api.dto.UserDto;
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
    @Value("${chess-api-opening-url}")
    private              String OPENINGS_URL;
    @Value("${chess-api-opening-url-tail}")
    private              String OPENINGS_TAIL;
    @Value("${high-win-rate}")
    private              Double HIGH_WIN_RATE;
    @Value("${low-win-rate}")
    private              Double LOW_WIN_RATE;
    @Value("${swift-amount-condition}")
    private              Double SWIFT_AMOUNT_CONDITION;
    private final static String MOVE_REGEX      = "(\\.\\\\u0020.\\\\u0020)|(\\.\\\\u0020..\\\\u0020)|(\\.\\\\u0020...\\\\u0020)|(\\.\\\\u0020....\\\\u0020)|(\\.\\\\u0020.....\\\\u0020)";
    private final static String PGN_REGEX       = "(pgn:).*";
    private final static String COLOR_REGEX     = "(White\\\\u0020\\\\u0022.+?(?=\\\\))|(Black\\\\u0020\\\\u0022.+?(?=\\\\))";
    private final static String WHITE_WON_REGEX = "(u002D0)";

    public GameServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<GameIdResponseDto> gamesByUserId(String userId) throws RuntimeException {
        try {
            return get(GAMES_URL + userId, ListGameIdResponses.class);
        } catch (RuntimeException e) {
            log.error("error while getting games by url {}", GAMES_URL);
        }
        return Collections.emptyList();
    }

    @Override
    public GameDto gameMoves(String gameId, String username) {
        GameDto game = new GameDto();
        game.setId(gameId);
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
                game.getMoves().add(moveDto);
            }
            pattern = Pattern.compile(WHITE_WON_REGEX);
            matcher = pattern.matcher(pgn);
            boolean whiteWon = matcher.find();
            game.setWon((whiteWon && white) || (!whiteWon && !white));
            return game;
        } catch (IOException e) {
            log.error("Parse html has an error by url: {}", GAME_INFO_URL + gameId);
            throw new RuntimeException(String.format("Jsoup connection failed for url: %s", GAME_INFO_URL + gameId));
        }
    }

    @Override
    public List<GameDto> getAllMoves(String username) {
        String userId = userService.getId(username);
        List<GameIdResponseDto> gameIds = gamesByUserId(userId);
        return gameIds.stream()
                      .map(gameId -> gameMoves(gameId.getId(), username))
                      .collect(Collectors.toList());
    }

    @Override
    public String getOpenings(String username) throws RuntimeException {
//        List<List<MoveDto>> games = getAllMoves(username);
//        for (List<MoveDto> moves: games) {
//
//        }
        try {
            String url = OPENINGS_URL + "e4" + OPENINGS_TAIL + "1&origMoves=e4";
            Document document = Jsoup.connect(url).get();
            Elements html = document.getAllElements();
            System.out.println(html);
        } catch (IOException e) {
            log.error("Parse html has an error by url: {}", OPENINGS_URL);
            throw new RuntimeException(String.format("Jsoup connection failed for url: %s", OPENINGS_URL));
        }
        return "Queen's gambit";
    }

    @Override
    public UserDto getStatistics(String username) {
        UserDto user = new UserDto();
        List<GameDto> games = getAllMoves(username);
        double wins = 0;
        double movesCount = 0;
        for (GameDto game : games) {
            if (game.isWon()) {
                wins++;
            }
            movesCount += game.getMoves().size() / 2.0;
        }
        user.setWinRate(wins / games.size());
        user.setHighWinRate(wins / games.size() > HIGH_WIN_RATE);
        user.setLowWinRate(wins / games.size() < LOW_WIN_RATE);
        user.setGoodMood(games.get(games.size() - 1).isWon() &&
                                 games.get(games.size() - 2).isWon() &&
                                 games.get(games.size() - 3).isWon());
        user.setBadMood(!games.get(games.size() - 1).isWon() &&
                                !games.get(games.size() - 2).isWon() &&
                                !games.get(games.size() - 3).isWon());
        movesCount /= games.size();
        user.setSwift(movesCount < SWIFT_AMOUNT_CONDITION);
        return user;
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}
