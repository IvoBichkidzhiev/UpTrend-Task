package org.example;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class ResultTestUtil {
    private ResultTestUtil() {}

    static FootballTeamsResponse response(int page, int perPage, int total, int totalPages, FootballTeam... clubs) {
        return new FootballTeamsResponse(page, perPage, total, totalPages, Arrays.asList(clubs));
    }

    static FootballTeam club(String name, String nation, long estimatedValueNumeric, long numberOfLeagueTitlesWon) {
        return new FootballTeam(name, nation, estimatedValueNumeric, numberOfLeagueTitlesWon);
    }

    static HttpServer createServer(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/api/football_teams", handler);
        server.start();
        return server;
    }

    static String readResource(String path) {
        InputStream inputStream = ResultTestUtil.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (content.length() > 0) {
                    content.append('\n');
                }
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + path, e);
        }
    }
}
