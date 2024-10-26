package com.kylehoehns.spring.ai.demo.function;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Map.entry;

@AllArgsConstructor
@Slf4j
public class LiveScoreFunction implements Function<LiveScoreFunction.ScoreRequest, LiveScoreFunction.ScoreResponse> {


    private static final Map<String, Integer> TEAM_NAME_TO_ID = Map.ofEntries(
            entry("Ohio State", 107),
            entry("Nebraska", 42),
            entry("Illinois", 47),
            entry("Oregon", 110),
            entry("Penn State", 78),
            entry("Wisconsin", 13),
            entry("Washington", 205),
            entry("Indiana", 103),
            entry("Michigan State", 92),
            entry("Michigan", 115),
            entry("Maryland", 140),
            entry("Minnesota", 85),
            entry("Northwestern", 41),
            entry("Iowa", 138)
    );


    private final RestClient restClient;

    @Override
    public ScoreResponse apply(ScoreRequest scoreRequest) {

        log.info("Live Score Request: {}", scoreRequest);

        var teamId = TEAM_NAME_TO_ID.get(scoreRequest.teamName());

        if (teamId == null) {
            throw new IllegalArgumentException("Unknown team: " + scoreRequest.teamName());
        }

        var response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/games")
                        .queryParam("team", teamId)
                        .queryParam("season", 2024)
                        .build())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<GamesResponse>() {
                });

        if (response.getBody() == null) {
            throw new RuntimeException("Could not get live score response");
        }

        var currentGame = mostRecentGame(response.getBody());

        var homeTeam = currentGame.teams().home().name();
        var homeScore = Optional.ofNullable(currentGame.scores.home().total()).orElse(0);

        var awayTeam = currentGame.teams().away().name();
        var awayScore = Optional.ofNullable(currentGame.scores.away().total()).orElse(0);

        return new ScoreResponse(homeTeam, homeScore, awayTeam, awayScore);
    }

    private GameResponse mostRecentGame(GamesResponse gamesResponse) {
        ZoneId central = ZoneId.of("America/Chicago");
        ZonedDateTime nowCST = ZonedDateTime.now(central);

        // Start of today in central time
        ZonedDateTime startOfToday = nowCST.toLocalDate().atStartOfDay(central);
        // End of today in central time
        ZonedDateTime endOfToday = startOfToday.plusDays(1).minusNanos(1);

        // First, check if there are any games played today
        Optional<GameResponse> gameToday = gamesResponse.response.stream()
                .filter(game -> {
                    ZonedDateTime gameDateCST = ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(game.game().date().timestamp()), central);
                    // Check if the game is scheduled for today
                    return !gameDateCST.isBefore(startOfToday) && !gameDateCST.isAfter(endOfToday);
                })
                .max(Comparator.comparing(game ->
                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(game.game().date().timestamp()), central)
                ));

        Supplier<GameResponse> mostRecentGameFromPast = () -> gamesResponse.response.stream()
                .filter(game -> {
                    ZonedDateTime gameDateCST = ZonedDateTime.ofInstant(
                            Instant.ofEpochSecond(game.game().date().timestamp()), central);
                    return gameDateCST.isBefore(nowCST);
                })
                .max(Comparator.comparing(game ->
                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(game.game().date().timestamp()), central)
                ))
                .orElse(null);

        return gameToday.orElseGet(mostRecentGameFromPast);
    }


    public record ScoreRequest(String teamName) {
    }

    public record ScoreResponse(String homeTeam, Integer homeScore, String awayTeam, Integer awayScore) {
    }

    private record GamesResponse(List<GameResponse> response) {
    }

    private record GameResponse(Game game, Teams teams, Scores scores) {
    }

    private record Game(int id, String stage, GameDate date) {
    }

    private record GameDate(String timezone, String date, String time, long timestamp) {
    }

    private record Teams(Team home, Team away) {
    }

    private record Team(String name) {
    }

    private record Scores(Score home, Score away) {
    }

    private record Score(Integer total) {
    }


}
