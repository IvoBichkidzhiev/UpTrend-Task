package org.example;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.example.ResultTestUtil.club;
import static org.example.ResultTestUtil.createServer;
import static org.example.ResultTestUtil.readResource;
import static org.example.ResultTestUtil.response;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultTest {

    @Test
    void deserializeResponseMapsSnakeCaseFields() {
        String json = readResource("/json/deserialize-response-maps-snake-case-fields.json");

        FootballTeamsResponse response = EliteClubListRestClient.deserializeResponse(new StringReader(json));

        assertEquals(1, response.page);
        assertEquals(10, response.perPage);
        assertEquals(1, response.total);
        assertEquals(1, response.totalPages);
        assertEquals(1, response.data.size());
        assertEquals("FC Example", response.data.get(0).name);
        assertEquals("England", response.data.get(0).nation);
        assertEquals(4200, response.data.get(0).estimatedValueNumeric);
        assertEquals(7, response.data.get(0).numberOfLeagueTitlesWon);
    }

    @Test
    void deserializeResponseReturnsEmptyResponseWhenJsonIsNull() {
        FootballTeamsResponse response = EliteClubListRestClient.deserializeResponse(new StringReader("null"));

        assertEquals(0, response.page);
        assertEquals(0, response.totalPages);
        assertTrue(response.data.isEmpty());
    }

    @Test
    void deserializeResponseNormalizesNullDataToEmptyList() {
        String json = readResource("/json/deserialize-response-normalizes-null-data-to-empty-list.json");

        FootballTeamsResponse response = EliteClubListRestClient.deserializeResponse(new StringReader(json));

        assertEquals(1, response.page);
        assertTrue(response.data.isEmpty());
    }

    @Test
    void deserializeResponseThrowsForMalformedJson() {
        assertThrows(IllegalStateException.class,
                () -> EliteClubListRestClient.deserializeResponse(new StringReader("{bad json")));
    }

    @Test
    void getAllFootballTeamsAggregatesAllPages() {
        FootballTeamsResponse firstPage = response(1, 2, 3, 2,
                club("Alpha FC", "Spain", 3000, 5),
                club("Beta FC", "Spain", 2000, 3));
        FootballTeamsResponse secondPage = response(2, 2, 3, 2,
                club("Gamma FC", "Spain", 2500, 4));

        Map<Integer, FootballTeamsResponse> pages = new HashMap<>();
        pages.put(1, firstPage);
        pages.put(2, secondPage);
        EliteClubListRestClient client = new PagingEliteClubListRestClient(pages);

        FootballTeamsResponse response = client.getAllFootballTeams();

        assertEquals(1, response.page);
        assertEquals(2, response.totalPages);
        assertEquals(3, response.data.size());
        assertEquals(Arrays.asList("Alpha FC", "Beta FC", "Gamma FC"),
                Arrays.asList(response.data.get(0).name, response.data.get(1).name, response.data.get(2).name));
    }

    @Test
    void getAllFootballTeamsHandlesNullDataPage() {
        FootballTeamsResponse firstPage = response(1, 2, 1, 2,
                club("Alpha FC", "Spain", 3000, 5));
        FootballTeamsResponse secondPage = new FootballTeamsResponse(2, 2, 1, 2, null);

        Map<Integer, FootballTeamsResponse> pages = new HashMap<>();
        pages.put(1, firstPage);
        pages.put(2, secondPage);

        FootballTeamsResponse response = new PagingEliteClubListRestClient(pages).getAllFootballTeams();

        assertEquals(1, response.page);
        assertEquals(2, response.totalPages);
        assertEquals(1, response.data.size());
        assertEquals("Alpha FC", response.data.get(0).name);
    }

    @Test
    void getAllFootballTeamsThrowsWhenAnyPageIsMissing() {
        FootballTeamsResponse firstPage = response(1, 2, 2, 3,
                club("Alpha FC", "Spain", 3000, 5));

        Map<Integer, FootballTeamsResponse> pages = new HashMap<>();
        pages.put(1, firstPage);
        pages.put(3, response(3, 2, 2, 3,
                club("Gamma FC", "Spain", 2500, 4)));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new PagingEliteClubListRestClient(pages).getAllFootballTeams());

        assertTrue(exception.getMessage().contains("page 2"));
    }

    @Test
    void getFootballTeamThrowsForHttpErrors() throws IOException {
        HttpServer server = createServer((exchange) -> {
            byte[] body = "error".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(body);
            }
        });

        try {
            URI baseUri = URI.create("http://localhost:" + server.getAddress().getPort()
                    + "/api/football_teams?nation=England");
            EliteClubListRestClient client = new EliteClubListRestClient(baseUri);

            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> client.getFootballTeam(1));
            assertTrue(exception.getMessage().contains("HTTP status: 500"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void eliteClubsFiltersSortsAndIncludesBoundaryMatches() {
        FootballTeam richClub = club("Zulu FC", "France", 9000, 8);
        FootballTeam tieBreakerClub = club("Alpha FC", "France", 5000, 4);
        FootballTeam sameValueClub = club("Omega FC", "France", 5000, 6);
        FootballTeam disqualifiedClub = club("Small FC", "France", 1000, 1);
        FootballTeam lowTitleClub = club("Almost FC", "France", 9000, 3);

        FootballTeamsResponse response = response(1, 5, 5, 1,
                richClub, tieBreakerClub, sameValueClub, disqualifiedClub, lowTitleClub);

        List<String> result = Result.eliteClubs(new FixedEliteClubListRestClient(response), 5000, 4);

        assertEquals(Arrays.asList("Zulu FC", "Alpha FC", "Omega FC"), result);
    }

    @Test
    void eliteClubsReturnsEmptyListWhenClubDataIsMissing() {
        FootballTeamsResponse response = new FootballTeamsResponse(1, 0, 0, 1, null);

        List<String> result = Result.eliteClubs(new FixedEliteClubListRestClient(response), 5000, 4);

        assertTrue(result.isEmpty());
    }

    @Test
    void eliteClubsReturnsEmptyListWhenClientIsNull() {
        List<String> result = Result.eliteClubs((EliteClubListRestClient) null, 5000, 4);

        assertTrue(result.isEmpty());
    }

    @Test
    void eliteClubsPublicOverloadReturnsEmptyListForNullNation() {
        List<String> result = Result.eliteClubs((String) null, 5000, 4);

        assertTrue(result.isEmpty());
    }

    @Test
    void eliteClubsSkipsNullOrUnnamedClubs() {
        FootballTeam unnamedClub = club(null, "France", 8000, 9);
        FootballTeam validClub = club("Valid FC", "France", 7000, 5);

        FootballTeamsResponse response = response(1, 3, 3, 1,
                null, unnamedClub, validClub);

        List<String> result = Result.eliteClubs(new FixedEliteClubListRestClient(response), 5000, 4);

        assertEquals(Collections.singletonList("Valid FC"), result);
    }

}
