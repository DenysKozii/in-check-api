package com.incheck.api.utils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Data
@Component
@RequiredArgsConstructor
@EnableScheduling
public class Scheduler {

//    @Value("${scheduler-url}")
//    private String URL;
//
//    @Scheduled(fixedRate = 1000 * 60 * 5)
//    public void timer() {
//        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
//            HttpUriRequest request = new HttpGet(URL);
//            client.execute(request);
//        }
//    }
}
