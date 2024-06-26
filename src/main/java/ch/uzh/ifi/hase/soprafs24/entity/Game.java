package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.GameStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GAME")
public class Game implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // If you want to auto-generate the ID
    private Long id;

    @OneToOne(mappedBy = "game")
    @JsonBackReference
    private Lobby lobby;

    @Enumerated(EnumType.STRING) // To store the enum as a string in the database
    @Column(nullable = false)
    private GameStatus status;

    @Column(name = "round_count", nullable = false)
    private Integer roundCount;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<Round> rounds = new ArrayList<>();

    @Column(columnDefinition = "text")
    private String gamePoints;  // This will store the JSON string

    public String getGamePoints() {
        return gamePoints;
    }

    public void setGamePoints(String pointsJson) {
        this.gamePoints = pointsJson;
    }

    // Getters and Setters

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Integer getRoundCount() {
        return roundCount;
    }

    public void setRoundCount(Integer roundCount) {
        this.roundCount = roundCount;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

}
