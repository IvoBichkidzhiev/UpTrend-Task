package org.example;

import java.net.URI;

final class FixedEliteClubListRestClient extends EliteClubListRestClient {
    private final FootballTeamsResponse response;

    FixedEliteClubListRestClient(FootballTeamsResponse response) {
        super(URI.create("http://localhost"));
        this.response = response;
    }

    @Override
    FootballTeamsResponse getAllFootballTeams() {
        return response;
    }
}
