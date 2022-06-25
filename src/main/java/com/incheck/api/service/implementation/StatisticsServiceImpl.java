package com.incheck.api.service.implementation;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.GamesResponseDto;
import com.incheck.api.dto.UserDto;
import com.incheck.api.dto.UserStatsDto;
import com.incheck.api.dto.UserStatsResponseDto;
import com.incheck.api.enums.Result;
import com.incheck.api.enums.TagInfo;
import com.incheck.api.service.StatisticsService;
import com.incheck.api.utils.AbstractHttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatisticsServiceImpl extends AbstractHttpClient implements StatisticsService {

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
    @Value("${high-accuracy-condition}")
    private              Double HIGH_ACCURACY_CONDITION;
    @Value("${day-millis}")
    private              Double DAY_MILLIS;
    @Value("${chess-api-user-stats-url}")
    private              String STATS_URL;
    private final static String OPENINGS_REGEX = "(openings/).+?(?=\")";
    private final static String MOVES_REGEX    = "([0-9]+\\. )";

    public StatisticsServiceImpl(RestTemplate restTemplate) {
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
    public UserDto getUserInfo(String username) {
        double wins = 0;
        int movesCount = 0;
        double surrendersCount = 0;
        double averageAccuracy = 0;
        int executionerWins = 0;
        boolean isWhite;
        boolean isBlack;
        UserDto user = new UserDto();
        ArrayList<String> openings = new ArrayList<>();
        UserStatsDto stats = getStats(username).getStats().get(0).getStats();
        List<GameDto> games = getAllGames(username).getGames();
        for (GameDto game :games) {
            isWhite = game.getWhite().getUsername().equals(username);
            if (game.getAccuracies() != null){
                averageAccuracy += isWhite ?
                                   game.getAccuracies().getWhite() :
                                   game.getAccuracies().getBlack();
                movesCount++;
            }
        }
        averageAccuracy /= movesCount;
        movesCount = 0;
        games = games.stream()
                     .filter(GameDto::isRated)
                     .sorted(Comparator.comparing(GameDto::getEndTime))
                     .collect(Collectors.toList())
                     .subList(games.size() - 30, games.size());
        Pattern movesPattern = Pattern.compile(MOVES_REGEX);
        Pattern openingsPattern = Pattern.compile(OPENINGS_REGEX);

        for (GameDto game : games) {
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
        movesCount /= games.size();
        surrendersCount /= games.size();

        user.getOpenings().addAll(sortOpenings(openings).subList(0, 3));
        user.setWinRate(wins / games.size());
        setUpTag(user, TagInfo.UNWARMED, System.currentTimeMillis() - DAY_MILLIS > games.get(0).getEndTime());
        setUpTag(user, TagInfo.HIGH_ACCURACY, averageAccuracy > HIGH_ACCURACY_CONDITION);
        setUpTag(user, TagInfo.HIGH_WIN_RATE, wins / games.size() > HIGH_WIN_RATE);
        setUpTag(user, TagInfo.LOW_WIN_RATE, wins / games.size() < LOW_WIN_RATE);
        setUpTag(user, TagInfo.SWIFT, movesCount < SWIFT_AMOUNT_CONDITION);
        setUpTag(user, TagInfo.UNDERVALUED, stats.getRatingTimeChangeValue() / stats.getRating() < UNDERVALUED_CONDITION);
        setUpTag(user, TagInfo.OVERVALUED, stats.getRatingTimeChangeValue() / stats.getRating() > OVERVALUED_CONDITION);
        setUpTag(user, TagInfo.NEVER_SURRENDER, surrendersCount < NEVER_SURRENDER_CONDITION);
        setUpTag(user, TagInfo.SURRENDERER, surrendersCount > SURRENDERER_CONDITION);
        setUpTag(user, TagInfo.EXECUTIONER, executionerWins > EXECUTIONER_CONDITION);
        setUpTag(user, TagInfo.GOOD_MOOD, games.get(games.size() - 1).isWon() &&
                games.get(games.size() - 2).isWon() &&
                games.get(games.size() - 3).isWon());
        setUpTag(user, TagInfo.BAD_MOOD, !games.get(games.size() - 1).isWon() &&
                !games.get(games.size() - 2).isWon() &&
                !games.get(games.size() - 3).isWon());

        return user;
    }

    private void setUpTag(UserDto user, TagInfo tag, boolean condition) {
        if (condition) {
            user.getTags().add(tag);
        }
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
    public UserStatsResponseDto getStats(String username) {
        try {
            return get(STATS_URL + username, UserStatsResponseDto.class);
        } catch (RuntimeException e) {
            log.error("error while getting user stats by url {}", STATS_URL + username);
        }
        return new UserStatsResponseDto();
    }

    @Override
    public HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept-language", "en-US");
        return headers;
    }

}