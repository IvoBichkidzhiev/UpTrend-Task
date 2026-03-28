package org.example;

class FootballTeam {
    /**
     * Football team name returned by the API.
     */
    final String name;

    /**
     * Nation that the football team belongs to.
     */
    final String nation;

    /**
     * Numeric football team valuation returned by the API.
     */
    final long estimatedValueNumeric;

    /**
     * Count of league titles won by the football team.
     */
    final long numberOfLeagueTitlesWon;

    FootballTeam(String name, String nation, long estimatedValueNumeric, long numberOfLeagueTitlesWon) {
        this.name = name;
        this.nation = nation;
        this.estimatedValueNumeric = estimatedValueNumeric;
        this.numberOfLeagueTitlesWon = numberOfLeagueTitlesWon;
    }
}
