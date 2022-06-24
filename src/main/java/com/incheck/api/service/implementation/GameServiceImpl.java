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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Value("${never-surrender-condition}")
    private              Double NEVER_SURRENDER_CONDITION;
    @Value("${surrenderer-condition}")
    private              Double SURRENDERER_CONDITION;
    @Value("${executioner-condition}")
    private              Double EXECUTIONER_CONDITION;
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
        ArrayList<String> openings = new ArrayList<>();
        UserStatsDto stats = userService.getStats(username).getStats().get(0).getStats();
        List<GameDto> games = getAllGames(username).getGames().stream()
                                                   .filter(GameDto::isRated)
                                                   .collect(Collectors.toList());
        Pattern movesPattern = Pattern.compile(MOVES_REGEX);
        Pattern openingsPattern = Pattern.compile(OPENINGS_REGEX);

        double wins = 0;
        int gamesCount = 0;
        int movesCount = 0;
        double surrendersCount = 0;
        int executionerWins = 0;
        boolean isWhite;
        boolean isBlack;
        for (GameDto game : games.subList(games.size() - 30, games.size())) {
            gamesCount++;
            isWhite = game.getWhite().getUsername().equals(username);
            isBlack = game.getBlack().getUsername().equals(username);
            game.setWon((isWhite && Result.WIN.getResult().equals(game.getWhite().getResult()))
                                || (isBlack && Result.WIN.getResult().equals(game.getBlack().getResult())));
            surrendersCount += (isWhite && Result.RESIGNED.getResult().equals(game.getWhite().getResult()))
                                       || (isBlack && Result.RESIGNED.getResult().equals(game.getBlack().getResult()))
                               ? 1 : 0;
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
                    String[] openingWords = openingsMatcher.group()
                                                           .replace("openings/", "")
                                                           .replace("...", "-")
                                                           .split("-");
                    opening = openingWords[0] + "-" + openingWords[1];
                    openings.add(opening);

                    movesCount += Integer.parseInt(moves);
                }
            if (games.indexOf(game) >= games.size() - 10 && game.isWon()) {
                executionerWins++;
            }
        }
        user.getOpenings().addAll(sortOpenings(openings).subList(0, 3));
        user.setWinRate(wins / gamesCount);
        tags.setHighWinRate(wins / gamesCount > HIGH_WIN_RATE);
        tags.setLowWinRate(wins / gamesCount < LOW_WIN_RATE);
        tags.setGoodMood(games.get(games.size() - 1).isWon() &&
                                 games.get(games.size() - 2).isWon() &&
                                 games.get(games.size() - 3).isWon());
        tags.setBadMood(!games.get(games.size() - 1).isWon() &&
                                !games.get(games.size() - 2).isWon() &&
                                !games.get(games.size() - 3).isWon());
        movesCount /= gamesCount;
        surrendersCount /= gamesCount;
        tags.setSwift(movesCount < SWIFT_AMOUNT_CONDITION);
        tags.setUndervalued(stats.getRatingTimeChangeValue() / stats.getRating() > OVERVALUED_CONDITION);
        tags.setOvervalued(stats.getRatingTimeChangeValue() / stats.getRating() < UNDERVALUED_CONDITION);
        tags.setNeverSurrender(surrendersCount < NEVER_SURRENDER_CONDITION);
        tags.setSurrenderer(surrendersCount > SURRENDERER_CONDITION);
        tags.setExecutioner(executionerWins > EXECUTIONER_CONDITION);
        user.setTags(tags);

        return user;
    }

    private List<String> sortOpenings(List<String> openings) {
        HashMap<String, Integer> countedOpenings = new HashMap<>();
        for (String opening : openings) {
            countedOpenings.merge(opening, 1, Integer::sum);
        }
        return countedOpenings.entrySet().stream()
                              .sorted(Map.Entry.comparingByValue())
                              .map(Map.Entry::getKey)
                              .sorted(Collections.reverseOrder())
                              .collect(Collectors.toList());
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}
