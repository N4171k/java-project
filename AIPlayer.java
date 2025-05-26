import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class AIPlayer {
    private int difficultyLevel;
    private boolean isWhite;
    private static final String API_KEY = "AIzaSyDuDNCV7iRJB4JYTiZEOEEM3gawkmqZl8U"; // Hardcoded API key
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final HttpClient httpClient;

    public AIPlayer(int difficultyLevel, boolean isWhite) {
        this.difficultyLevel = difficultyLevel;
        this.isWhite = isWhite;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getNextMove(List<String> movesHistory) {
        try {
            String prompt = buildPrompt(movesHistory);
            String response = makeApiCall(prompt);
            return parseMoveFromResponse(response);
        } catch (Exception e) {
            System.err.println("Error getting AI move: " + e.getMessage());
            return "e2e4"; // Default move in case of error
        }
    }

    private String makeApiCall(String prompt) throws Exception {
        JSONObject requestBody = new JSONObject();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONObject part = new JSONObject();
        part.put("text", prompt);
        parts.put(part);
        content.put("parts", parts);
        requestBody.put("contents", new JSONArray().put(content));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(GEMINI_API_URL + "?key=" + API_KEY))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API call failed with status code: " + response.statusCode() + " and body: " + response.body());
        }

        return response.body();
    }

    private String parseMoveFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    String move = parts.getJSONObject(0).getString("text").trim();
                    // Validate move format
                    if (isValidMoveFormat(move)) {
                        return move;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing API response: " + e.getMessage());
        }
        return "e2e4"; // Default move if parsing fails
    }

    private boolean isValidMoveFormat(String move) {
        // Basic validation for common move formats
        return move.matches("^[a-h][1-8][a-h][1-8]$") || // e2e4
               move.matches("^[KQRBN][a-h][1-8]$") || // Nf3
               move.matches("^O-O$") || // Kingside castle
               move.matches("^O-O-O$"); // Queenside castle
    }

    private String buildPrompt(List<String> movesHistory) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a chess AI playing at difficulty level ").append(difficultyLevel).append(".\n");
        prompt.append("You are playing as ").append(isWhite ? "white" : "black").append(".\n");
        prompt.append("The current game history is:\n");

        for (int i = 0; i < movesHistory.size(); i++) {
            prompt.append(i + 1).append(". ").append(movesHistory.get(i)).append("\n");
        }

        prompt.append("\nPlease provide your next move in algebraic notation (e.g., e2e4, Nf3, O-O).\n");
        prompt.append("Consider the difficulty level when making your move - higher levels should play more strategically.\n");
        prompt.append("Only respond with the move in algebraic notation, nothing else.");

        return prompt.toString();
    }

    public void setDifficultyLevel(int level) {
        if (level < 1 || level > 10) {
            throw new IllegalArgumentException("Difficulty level must be between 1 and 10");
        }
        this.difficultyLevel = level;
    }
}
