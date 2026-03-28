package org.example;

import java.net.URI;
import java.util.Map;

final class PagingEliteClubListRestClient extends EliteClubListRestClient {
    private final Map<Integer, FootballTeamsResponse> pages;

    PagingEliteClubListRestClient(Map<Integer, FootballTeamsResponse> pages) {
        super(URI.create("http://localhost"));
        this.pages = pages;
    }

    @Override
    FootballTeamsResponse getFootballTeam(int page) {
        return pages.get(page);
    }
}
