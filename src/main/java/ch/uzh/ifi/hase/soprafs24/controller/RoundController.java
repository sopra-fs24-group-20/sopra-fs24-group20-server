package ch.uzh.ifi.hase.soprafs24.controller;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Round;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs24.service.RoundService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RoundController {
    private final RoundService roundService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private GameRepository gameRepository;
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public RoundController(RoundService roundService, GameRepository gameRepository) {
        this.roundService = roundService;
        this.gameRepository = gameRepository;
    }
    @GetMapping("/rounds/{gameId}")
    public ResponseEntity<List<Round>> getRoundsByGameId(@PathVariable Long gameId) {
        List<Round> rounds = roundService.getRoundByGameId(gameId);
        if (rounds != null && !rounds.isEmpty()) {
            return new ResponseEntity<>(rounds, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/rounds/{gameId}/entries")
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ResponseEntity<String> addGameEntry(@PathVariable Long gameId, @RequestBody Map<String, String> gameEntry) {
        try {
            Round currentRound = roundService.getCurrentRoundByGameId(gameId);
            if (currentRound != null) {
                String entryJson = objectMapper.writeValueAsString(gameEntry);
                String existingAnswers = currentRound.getPlayerAnswers();
                String updatedAnswers = existingAnswers == null ? entryJson : existingAnswers + "," + entryJson;
                currentRound.setPlayerAnswers(updatedAnswers);
                roundService.saveRound(currentRound);
                return ResponseEntity.ok("{\"message\":\"Entry added successfully.\"}");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\":\"Failed to serialize entry.\"}");
        }
    }

    @GetMapping("/rounds/letters/{gameId}")
    public ResponseEntity<Character> getLetter(@PathVariable Long gameId) {
        char currentLetter = roundService.getCurrentRoundLetter(gameId);
        if (currentLetter != '\0') {
            return new ResponseEntity<>(currentLetter, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/rounds/letterPosition/{gameId}")
    public  ResponseEntity<Integer> getLetterPosition(@PathVariable Long gameId) {
        int currentLetterPosition = roundService.getCurrentRoundLetterPosition(gameId);
        if (currentLetterPosition != -100) {
            return new ResponseEntity<>(currentLetterPosition, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/rounds/leaderboard/{gameId}")
    public ResponseEntity<Map<String, Integer>> getLeaderboard(@PathVariable Long gameId) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if (optionalGame.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Game game = optionalGame.get();
        try {
            String existingGamePointsJson = game.getGamePoints();
            Map<String, Integer> gamePoints;
            if (existingGamePointsJson != null && !existingGamePointsJson.isEmpty()) {
                gamePoints = objectMapper.readValue(existingGamePointsJson, new TypeReference<Map<String, Integer>>() {
                });
                return ResponseEntity.ok(gamePoints.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1, // if there are duplicates, keep the existing
                                LinkedHashMap::new
                        )));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/rounds/scores/{gameId}")
    public ResponseEntity<?> getScoresByCategory(@PathVariable Long gameId) {
        try {
            Map<String, Map<String, Map<String, Object>>> scoresAndAnswersByCategory = roundService.calculateScoresCategory(gameId);
            return ResponseEntity.ok(scoresAndAnswersByCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/rounds/{gameId}/submitVotes")
    public ResponseEntity<?> submitVotes(@PathVariable Long gameId, @RequestBody String rawJson) {
        try {
            // Parse the raw JSON into the structured format
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<HashMap<String, HashMap<String, HashMap<String, Object>>>> typeRef =
                    new TypeReference<>() {};
            HashMap<String, HashMap<String, HashMap<String, Object>>> votes = objectMapper.readValue(rawJson, typeRef);

            // Update the vote counts in the game's current round
            Map<String, Map<String, Map<String, Object>>> voteUpdates = roundService.prepareScoreAdjustments(gameId, votes);
            return ResponseEntity.ok(voteUpdates);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid JSON format: " + e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();  // Log runtime exceptions to help diagnose issues.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();  // Log unexpected exceptions.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @GetMapping("/rounds/score-difference/{gameId}")
    public ResponseEntity<Map<String, Integer>> getScoreDifference(@PathVariable Long gameId) {
        try {
            Map<String, Integer> scoreDifference = roundService.calculateScoreDifference(gameId);
            return new ResponseEntity<>(scoreDifference, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
