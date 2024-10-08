package com.kylehoehns.spring.ai.demo.function;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class BestTeamFunction implements Function<BestTeamFunction.Request, BestTeamFunction.Response> {

    @Override
    public Response apply(Request request) {

        log.info("Best Team Request: {}", request);

        return new Response("Iowa Hawkeyes");
    }

    public record Request(String question) {}
    public record Response(String team) {}
}
