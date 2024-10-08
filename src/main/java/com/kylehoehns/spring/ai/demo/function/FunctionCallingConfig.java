package com.kylehoehns.spring.ai.demo.function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@Configuration
public class FunctionCallingConfig {

    @Bean
    public RestClient restClient(@Value("${sports.api.key}") String apiKey) {
        return RestClient.builder()
                .baseUrl("https://v1.american-football.api-sports.io")
                .defaultHeaders(headers -> {
                    headers.add("x-rapidapi-key", apiKey);
                    headers.add("x-rapidapi-host", "v1.american-football.api-sports.io");
                })
                .build();
    }

    @Bean
    @Description("Get the best team in college football")
    public Function<BestTeamFunction.Request, BestTeamFunction.Response> bestTeam() {
        return new BestTeamFunction();
    }

    @Bean
    @Description("Get the live score of a current game")
    public Function<LiveScoreFunction.ScoreRequest, LiveScoreFunction.ScoreResponse> liveScore(RestClient restClient) {
        return new LiveScoreFunction(restClient);
    }

}
