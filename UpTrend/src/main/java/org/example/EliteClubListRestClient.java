package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class EliteClubListRestClient {
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 5_000;

    /**
     * Gson instance configured to map snake_case JSON fields to Java fields.
     */
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    /**
     * Base URI used to request football team data from the remote API.
     */
    private final URI baseUri;

    EliteClubListRestClient(URI baseUri) {
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
    }

    FootballTeamsResponse getAllFootballTeams() {
        FootballTeamsResponse firstPageResponse = getFootballTeam(1);
        if (firstPageResponse == null) {
            throw new IllegalStateException("Missing response for clubs page 1.");
        }
        FootballTeamsResponse firstPage = normalizeResponse(firstPageResponse);
        List<FootballTeam> allClubs = new ArrayList<>(firstPage.data);

        for (int page = 2; page <= firstPage.totalPages; page++) {
            FootballTeamsResponse pageResponse = getFootballTeam(page);
            if (pageResponse == null) {
                throw new IllegalStateException("Missing response for clubs page " + page + ".");
            }
            allClubs.addAll(normalizeResponse(pageResponse).data);
        }

        return new FootballTeamsResponse(
                firstPage.page,
                firstPage.perPage,
                firstPage.total,
                firstPage.totalPages,
                allClubs
        );
    }

    FootballTeamsResponse getFootballTeam(int page) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be positive.");
        }

        HttpURLConnection connection = null;
        try {
            String baseUriText = baseUri.toString();
            String separator = baseUriText.contains("?") ? "&" : "?";
            URI pageUri = URI.create(baseUriText + separator + "page=" + page);
            URL pageUrl = pageUri.toURL();
            connection = (HttpURLConnection) pageUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException(
                        "Failed to fetch clubs page " + page + ". HTTP status: " + statusCode);
            }

            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            )) {
                return deserializeResponse(bufferedReader);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch clubs page " + page + ".", exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static FootballTeamsResponse deserializeResponse(Reader reader) {
        try {
            ApiEliteClubListResponse apiResponse = GSON.fromJson(reader, ApiEliteClubListResponse.class);
            return normalizeResponse(toModelResponse(apiResponse));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Failed to deserialize club response.", exception);
        }
    }

    private static FootballTeamsResponse normalizeResponse(FootballTeamsResponse response) {
        if (response == null) {
            return FootballTeamsResponse.empty();
        }
        if (response.data == null) {
            return new FootballTeamsResponse(
                    response.page,
                    response.perPage,
                    response.total,
                    response.totalPages,
                    new ArrayList<>()
            );
        }
        return response;
    }

    private static FootballTeamsResponse toModelResponse(ApiEliteClubListResponse apiResponse) {
        if (apiResponse == null) {
            return FootballTeamsResponse.empty();
        }

        List<FootballTeam> clubs = null;
        if (apiResponse.data != null) {
            clubs = new ArrayList<>(apiResponse.data.size());
            for (ApiEliteClub apiClub : apiResponse.data) {
                if (apiClub == null) {
                    clubs.add(null);
                    continue;
                }
                clubs.add(new FootballTeam(
                        apiClub.name,
                        apiClub.nation,
                        apiClub.estimatedValueNumeric,
                        apiClub.numberOfLeagueTitlesWon
                ));
            }
        }

        return new FootballTeamsResponse(
                apiResponse.page,
                apiResponse.perPage,
                apiResponse.total,
                apiResponse.totalPages,
                clubs
        );
    }

    private static final class ApiEliteClubListResponse {
        int page;
        int perPage;
        int total;
        int totalPages;
        List<ApiEliteClub> data;
    }

    private static final class ApiEliteClub {
        String name;
        String nation;
        long estimatedValueNumeric;
        long numberOfLeagueTitlesWon;
    }
}
