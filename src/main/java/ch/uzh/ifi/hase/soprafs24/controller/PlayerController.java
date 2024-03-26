package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.DebugWindow;
import ch.uzh.ifi.hase.soprafs24.entity.Player;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.PlayerService;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.PlayerPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Player Controller
 * This class is responsible for handling all REST request that are related to
 * the Player.
 * The controller will receive the request and delegate the execution to the
 * PlayerService and finally return the result.
 */
@RestController
public class PlayerController {

  private final PlayerService PlayerService;

  PlayerController(PlayerService PlayerService) {
    this.PlayerService = PlayerService;
  }

    @GetMapping("/players")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<PlayerGetDTO> getAllPlayers() {
        // Fetch all Players in the internal representation
        List<Player> players = PlayerService.getPlayers();
        List<PlayerGetDTO> playerGetDTOs = new ArrayList<>();

        // Convert each Player to the API representation
        for (Player player : players) {
            playerGetDTOs.add(DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(player));
        }
        return playerGetDTOs;
    }
    @PutMapping("/players/{username}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO updatePlayer(@PathVariable String username, @RequestBody PlayerPutDTO playerPutDTO) {
        // Convert DTO to entity
        Player updatedPlayerInfo = DTOMapper.INSTANCE.convertPlayerPutDTOtoEntity(playerPutDTO);

        // Call service to update player
        Player updatedPlayer = PlayerService.updatePlayer(username, updatedPlayerInfo);

        // Convert updated entity back to DTO
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(updatedPlayer);
    }

    @PostMapping("/players")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PlayerGetDTO createPlayer(@RequestBody PlayerPostDTO playerPostDTO) {
        if (playerPostDTO.getUsername() == null || playerPostDTO.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must not be empty");
        }

        Player playerInput = DTOMapper.INSTANCE.convertPlayerPostDTOtoEntity(playerPostDTO);
        Player createdPlayer = PlayerService.createPlayer(playerInput.getUsername(), playerInput.getPassword());
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(createdPlayer);
    }


    @GetMapping("/players/{Username}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO getPlayerByUsername(@PathVariable String Username) {
        // fetch Player by Username
        Player Player = PlayerService.getPlayerByUsername(Username);
        if (Player == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with Username: " + Username);
        }
        // convert Player to the API representation
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(Player);
    }

    {/*
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlayerGetDTO login(@RequestBody PlayerPostDTO PlayerPostDTO) {
        // Retrieve Player from database based on the Playername
        Player Player = PlayerService.authenticate(PlayerPostDTO.getPlayername(), PlayerPostDTO.getPassword());
        return DTOMapper.INSTANCE.convertEntityToPlayerGetDTO(Player);
    }

  @PutMapping("/logout/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
    public void logout(@PathVariable Long id){
      PlayerService.updatePlayerStatus(id, PlayerStatus.OFFLINE);

  }


    @PutMapping("/Players/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void setPlayernameBirthdate(@PathVariable Long id, @RequestBody Map<String, Object> requestBody) {

      String Playername = (String) requestBody.get("Playername");
        String birthdateString = (String) requestBody.get("birthdate");

        LocalDate birthdate = null;
        if (birthdateString != null && !birthdateString.isEmpty()) {
            birthdate = LocalDate.parse(birthdateString);
        }
        Player Player = PlayerService.getPlayerById(id);
        if (Player == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with id: " + id);}

        PlayerService.updatePlayernameBirthdate(id, Playername, birthdate);
    }
*/}
}
