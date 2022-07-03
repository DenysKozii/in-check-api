package com.incheck.api.service.implementation;

import com.incheck.api.dto.GameDto;
import com.incheck.api.dto.GamesResponseDto;
import com.incheck.api.dto.OpeningSuggestDto;
import com.incheck.api.dto.UserDto;
import com.incheck.api.dto.UserStatsDto;
import com.incheck.api.dto.UserStatsResponseDto;
import com.incheck.api.enums.Result;
import com.incheck.api.enums.TagInfo;
import com.incheck.api.service.StatisticsService;
import com.incheck.api.utils.AbstractHttpClient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    @Value("${day-seconds}")
    private              Double DAY_SECONDS;
    @Value("${chess-api-user-stats-url}")
    private              String STATS_URL;
    @Value("${openings-directory}")
    private              String OPENINGS_DIRECTORY;
    private final static String OPENINGS_REGEX     = "(openings/).+?(?=\")";
    private final static String MOVES_NUMBER_REGEX = "([0-9]+\\. )";
    private final static String MOVES_REGEX        = "(([0-9]+\\. )|([0-9]+\\... ))(.+?(?= ))";

    private final Map<String, OpeningSuggestDto> whiteOpenings = new TreeMap<>(Comparator.comparingInt(String::length).reversed());
    private final Map<String, OpeningSuggestDto> blackOpenings = new TreeMap<>(Comparator.comparingInt(String::length).reversed());

    public StatisticsServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }


    @EventListener(ApplicationReadyEvent.class)
    public void setup() {
        JSONParser jsonParser = new JSONParser();
        try {
            Object obj = jsonParser.parse(new FileReader(OPENINGS_DIRECTORY));
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject openings = (JSONObject) jsonObject.get("openings");
            JSONArray whiteSuggestions = (JSONArray) openings.get("whiteSuggestions");
            JSONArray blackSuggestions = (JSONArray) openings.get("blackSuggestions");
            fillOpenings(whiteSuggestions, whiteOpenings);
            fillOpenings(blackSuggestions, blackOpenings);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void fillOpenings(JSONArray jsonArray, Map<String, OpeningSuggestDto> openings) {
        for (Object jsonObj : jsonArray) {
            JSONObject json = (JSONObject) jsonObj;
            JSONObject opponent = (JSONObject) json.get("opponent");
            JSONObject suggest = (JSONObject) json.get("suggest");
            String moves = opponent.get("moves").toString();
            String suggestMoves = suggest.get("moves").toString();
            String title = opponent.get("title").toString();
            String suggestTitle = suggest.get("title").toString();
            OpeningSuggestDto openingSuggest = new OpeningSuggestDto(title, suggestTitle, suggestMoves);
            openings.put(moves, openingSuggest);
        }
    }

    @Override
    public GamesResponseDto getLastGames(String username) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        String formattedMonth = month > 10 ? String.valueOf(month) : "0" + month;
        String url = GAMES_MONTH_URL + username + "/games/" + year + "/" + formattedMonth;
        try {
            GamesResponseDto response = get(url, GamesResponseDto.class);
            if (response.getGames().size() < 30) {
                month = c.get(Calendar.MONTH);
                if (month == 0) {
                    year--;
                    month = 12;
                }
                formattedMonth = month > 10 ? String.valueOf(month) : "0" + month;
                url = GAMES_MONTH_URL + username + "/games/" + year + "/" + formattedMonth;
                GamesResponseDto lastMonthGames = get(url, GamesResponseDto.class);
                response.getGames().addAll(lastMonthGames.getGames());
            }
            return response;
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
        Map<OpeningSuggestDto, Integer> suggests = new HashMap<>();
        UserDto user = new UserDto();
        UserStatsDto stats = getStats(username).getStats().get(0).getStats();
        List<GameDto> games = getLastGames(username).getGames();
        for (GameDto game :games) {
            isWhite = game.getWhite().getUsername().equalsIgnoreCase(username);
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
                     .collect(Collectors.toList());
        Pattern movesCounterPattern = Pattern.compile(MOVES_NUMBER_REGEX);
        Pattern movesPattern = Pattern.compile(MOVES_REGEX);

        for (GameDto game : games) {
            isWhite = game.getWhite().getUsername().equalsIgnoreCase(username);
            isBlack = game.getBlack().getUsername().equalsIgnoreCase(username);
            user.setUsername(isWhite ? game.getWhite().getUsername() : game.getBlack().getUsername());
            game.setWon((isWhite && Result.WIN.getResult().equals(game.getWhite().getResult()))
                                || (isBlack && Result.WIN.getResult().equals(game.getBlack().getResult())));
            surrendersCount += (isWhite && Result.RESIGNED.getResult().equals(game.getWhite().getResult()))
                                       || (isBlack && Result.RESIGNED.getResult().equals(game.getBlack().getResult()))
                               ? 1 : 0;
            if (game.isWon()) {
                wins++;
            } else if ((isWhite && Result.REPETITION.getResult().equals(game.getWhite().getResult()))
                    || (isBlack && Result.REPETITION.getResult().equals(game.getBlack().getResult()))
                    || (isWhite && Result.AGREED.getResult().equals(game.getWhite().getResult()))
                    || (isBlack && Result.AGREED.getResult().equals(game.getBlack().getResult()))) {
                user.setDraws(user.getDraws() + 1);
            } else {
                user.setLoses(user.getLoses() + 1);
            }
            Matcher lastMoveNumberMatcher = movesCounterPattern.matcher(game.getPgn());
            Matcher movesMatcher = movesPattern.matcher(game.getPgn());
            String lastMovesNumber = "1";
            StringBuilder moves = new StringBuilder();
            while (lastMoveNumberMatcher.find()) {
                lastMovesNumber = lastMoveNumberMatcher.group().replace(". ", "");
            }
            while (movesMatcher.find()) {
                moves.append(movesMatcher
                                     .group().replaceAll(Pattern.compile("(([0-9]+\\. )|([0-9]+\\... ))").pattern(), ""))
                     .append(" ");
            }
            Map<String, OpeningSuggestDto> openings = isWhite ? whiteOpenings : blackOpenings;
            for (String openingMoves : openings.keySet()) {
                if (moves.toString().contains(openingMoves)) {
                    suggests.merge(openings.get(openingMoves), 1, Integer::sum);
                    break;
                }
            }
            movesCount += Integer.parseInt(lastMovesNumber);
            if (games.indexOf(game) >= games.size() - 10 && game.isWon()) {
                executionerWins++;
            }
        }
        List<OpeningSuggestDto> openings = suggests.entrySet().stream()
                                                   .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                                                   .map(Map.Entry::getKey)
                                                   .collect(Collectors.toList());
        user.setOpenings(openings.subList(0, Math.min(3, openings.size())));
        movesCount /= games.size();
        surrendersCount /= games.size();
        user.setWinRate(wins / games.size());
        user.setWins((int) wins);
        setUpTag(user, TagInfo.UNWARMED, Instant.now().getEpochSecond() - DAY_SECONDS > games.get(games.size() - 1).getEndTime());
        setUpTag(user, TagInfo.HIGH_ACCURACY, averageAccuracy > HIGH_ACCURACY_CONDITION);
        setUpTag(user, TagInfo.HIGH_WIN_RATE, wins / games.size() > HIGH_WIN_RATE);
        setUpTag(user, TagInfo.LOW_WIN_RATE, wins / games.size() < LOW_WIN_RATE);
        setUpTag(user, TagInfo.SWIFT, movesCount < SWIFT_AMOUNT_CONDITION);
        setUpTag(user, TagInfo.UNDERVALUED, stats.getRatingTimeChangeValue() / stats.getRating() < UNDERVALUED_CONDITION);
        setUpTag(user, TagInfo.OVERVALUED, stats.getRatingTimeChangeValue() / stats.getRating() > OVERVALUED_CONDITION);
        setUpTag(user, TagInfo.INACTIVE, games.size() < 10);
        setUpTag(user, TagInfo.NEVER_SURRENDER, surrendersCount < NEVER_SURRENDER_CONDITION);
        setUpTag(user, TagInfo.SURRENDERER, surrendersCount > SURRENDERER_CONDITION);
        setUpTag(user, TagInfo.EXECUTIONER, executionerWins > EXECUTIONER_CONDITION);
        setUpTag(user, TagInfo.GOOD_MOOD, games.size() > 3 &&
                games.get(games.size() - 1).isWon() &&
                games.get(games.size() - 2).isWon() &&
                games.get(games.size() - 3).isWon());
        setUpTag(user, TagInfo.BAD_MOOD, games.size() > 3 &&
                !games.get(games.size() - 1).isWon() &&
                !games.get(games.size() - 2).isWon() &&
                !games.get(games.size() - 3).isWon());
        return user;
    }

    private void setUpTag(UserDto user, TagInfo tag, boolean condition) {
        if (condition) {
            user.getTags().add(tag);
        }
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
