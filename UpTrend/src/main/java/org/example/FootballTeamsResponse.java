package org.example;

import java.util.ArrayList;
import java.util.List;

class FootballTeamsResponse {
    /**
     * Current page number returned by the API.
     */
    final int page;

    /**
     * Maximum number of records returned on one page.
     */
    final int perPage;

    /**
     * Total number of records across all pages.
     */
    final int total;

    /**
     * Total number of available pages.
     */
    final int totalPages;

    /**
     * Football teams returned for the current page.
     */
    final List<FootballTeam> data;

    FootballTeamsResponse(int page, int perPage, int total, int totalPages, List<FootballTeam> data) {
        this.page = page;
        this.perPage = perPage;
        this.total = total;
        this.totalPages = totalPages;
        this.data = data;
    }

    static FootballTeamsResponse empty() {
        return new FootballTeamsResponse(0, 0, 0, 0, new ArrayList<>());
    }
}
