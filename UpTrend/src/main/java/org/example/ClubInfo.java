package org.example;

class ClubInfo {
    /**
     * Name of the football team that matched the filtering criteria.
     */
    final String clubName;

    /**
     * Monetary value used to sort qualified football teams.
     */
    final long estimatedValue;

    ClubInfo(String clubName, long estimatedValue) {
        this.clubName = clubName;
        this.estimatedValue = estimatedValue;
    }
}
