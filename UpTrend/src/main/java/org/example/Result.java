package org.example;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Result {

    public static List<String> eliteClubs(String nation, int minValuation, int minTitlesWon) {
        if (nation == null || nation.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String encodedNation = URLEncoder.encode(nation, StandardCharsets.UTF_8.toString());
            URI baseUri = URI.create("https://jsonmock.hackerrank.com/api/football_teams?nation=" + encodedNation);
            EliteClubListRestClient client = new EliteClubListRestClient(baseUri);
            return eliteClubs(client, minValuation, minTitlesWon);
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalStateException("UTF-8 encoding is required but unavailable.", exception);
        }
    }

    static List<String> eliteClubs(EliteClubListRestClient client, int minValuation, int minTitlesWon) {
        if (client == null) {
            return new ArrayList<>();
        }

        List<ClubInfo> qualifiedClubs = new ArrayList<>();
        FootballTeamsResponse allClubs = client.getAllFootballTeams();
        if (allClubs == null || allClubs.data == null) {
            return new ArrayList<>();
        }

        for (FootballTeam club : allClubs.data) {
            if (club == null || club.name == null || club.name.trim().isEmpty()) {
                continue;
            }
            if (club.estimatedValueNumeric >= minValuation && club.numberOfLeagueTitlesWon >= minTitlesWon) {
                qualifiedClubs.add(new ClubInfo(club.name, club.estimatedValueNumeric));
            }
        }

        qualifiedClubs.sort(Comparator.comparingLong((ClubInfo _club) -> _club.estimatedValue).reversed()
                .thenComparing(_club -> _club.clubName));

        List<String> result = new ArrayList<>();
        for (ClubInfo club : qualifiedClubs) {
            result.add(club.clubName);
        }

        return result;
    }
}
