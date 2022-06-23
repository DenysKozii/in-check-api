package com.incheck.api.service.implementation;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.GamesResponseDto;
import com.incheck.api.dto.TagsDto;
import com.incheck.api.dto.UserDto;
import com.incheck.api.dto.UserStatsDto;
import com.incheck.api.enums.Result;
import com.incheck.api.service.GameService;
import com.incheck.api.service.UserService;
import com.incheck.api.utils.AbstractHttpClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class GameServiceImpl extends AbstractHttpClient implements GameService {

    @Autowired
    private UserService userService;

    @Value("${chess-api-games-month-url}")
    private              String GAMES_MONTH_URL;
    @Value("${high-win-rate}")
    private              Double HIGH_WIN_RATE;
    @Value("${low-win-rate}")
    private              Double LOW_WIN_RATE;
    @Value("${swift-amount-condition}")
    private              Double SWIFT_AMOUNT_CONDITION;
    @Value("${undervalued-condition}")
    private              Double UNDERVALUED_CONDITION;
    @Value("${overvalued-condition}")
    private              Double OVERVALUED_CONDITION;
    private final static String OPENINGS_REGEX = "(openings/).+?(?=\")";
    private final static String MOVES_REGEX    = "([0-9]+\\. )";

    public GameServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public GamesResponseDto getAllGames(String username) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        String formattedMonth = month > 10 ? String.valueOf(month) : "0" + month;
        String url = GAMES_MONTH_URL + username + "/games/" + year + "/" + formattedMonth;
        try {
            return get(url, GamesResponseDto.class);
        } catch (RuntimeException e) {
            log.error("error while getting games by url {}", url);
        }
        return new GamesResponseDto();
    }

    @Override
    public UserDto getStatistics(String username) {
        UserDto user = new UserDto();
        TagsDto tags = new TagsDto();
        UserStatsDto stats = userService.getStats(username).getStats().get(0).getStats();
        List<GameDto> games = getAllGames(username).getGames();
        Pattern movesPattern = Pattern.compile(MOVES_REGEX);
        Pattern openingsPattern = Pattern.compile(OPENINGS_REGEX);

        double wins = 0;
        double movesCount = 0;
        for (GameDto game : games) {
            game.setWon((game.getWhite().getUsername().equals(username)
                    && Result.WIN.getResult().equals(game.getWhite().getResult()))
                                || (game.getBlack().getUsername().equals(username)
                    && Result.WIN.getResult().equals(game.getBlack().getResult())));
            if (game.isWon()) {
                wins++;
            }
            Matcher movesMatcher = movesPattern.matcher(game.getPgn());
            Matcher openingsMatcher = openingsPattern.matcher(game.getPgn());
            String opening;
            String moves = "1";
            while (movesMatcher.find()) {
                moves = movesMatcher.group().replace(". ", "");
            }
            if (openingsMatcher.find()) {
                opening = openingsMatcher.group().replace("openings/", "");
                user.getOpenings().add(opening);
            }
            movesCount += Integer.parseInt(moves);
        }
        user.setWinRate(wins / games.size());

        tags.setHighWinRate(wins / games.size() > HIGH_WIN_RATE);
        tags.setLowWinRate(wins / games.size() < LOW_WIN_RATE);
        tags.setGoodMood(games.get(games.size() - 1).isWon() &&
                                 games.get(games.size() - 2).isWon() &&
                                 games.get(games.size() - 3).isWon());
        tags.setBadMood(!games.get(games.size() - 1).isWon() &&
                                !games.get(games.size() - 2).isWon() &&
                                !games.get(games.size() - 3).isWon());
        movesCount /= games.size();
        tags.setSwift(movesCount < SWIFT_AMOUNT_CONDITION);
        tags.setUndervalued(stats.getRatingTimeChangeValue() / stats.getRating() > OVERVALUED_CONDITION);
        tags.setOvervalued(stats.getRatingTimeChangeValue() / stats.getRating() < UNDERVALUED_CONDITION);
        user.setTags(tags);

        return user;
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}
