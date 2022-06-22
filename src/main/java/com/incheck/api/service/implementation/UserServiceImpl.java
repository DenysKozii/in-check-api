package com.incheck.api.service.implementation;

import com.incheck.api.dto.UserStatsResponseDto;
import com.incheck.api.service.UserService;
import com.incheck.api.utils.AbstractHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserServiceImpl extends AbstractHttpClient implements UserService {

    @Value("${chess-api-stats-url}")
    private String ID_URL;
    @Value("${chess-api-user-stats-url}")
    private String STATS_URL;
    private final static String USER_ID_REGEX = "(data-user-id=\"\\d*\")";

    public UserServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public String getId(String username) throws RuntimeException {
        try {
            Document document = Jsoup.connect(ID_URL + username).get();
            Elements html = document.getAllElements();
            Pattern pattern = Pattern.compile(USER_ID_REGEX);
            Matcher matcher = pattern.matcher(html.toString());
            if (matcher.find()) {
                return matcher.group()
                              .replace("\"","")
                              .replace("data-user-id=","");
            }
        } catch (IOException e) {
            log.error("Parse html has an error by url: {}", ID_URL + username);
            throw new RuntimeException(String.format("Jsoup connection failed for url: %s", ID_URL + username));
        }
        return "";
    }

    @Override
    public UserStatsResponseDto getStats(String username) {
        try {
            return get(STATS_URL + username, UserStatsResponseDto.class);
        } catch (RuntimeException e) {
            log.error("error while getting user stats by url {}", STATS_URL+username);
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
