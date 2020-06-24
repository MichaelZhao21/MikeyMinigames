package xyz.michaelzhao.mikeyminigames.games;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

enum GameType {MULTIPLAYER, TEAM, GROUP, SINGLEPLAYER}

enum DeathCondition {NONE, FALLING, HEALTH}

enum WinCondition {NONE, LAST_ALIVE, LAST_TEAM_ALIVE, HIGHEST_SCORE, TIMER, ENDLESS, LAST_CHECKPOINT, LAST_ROUND}

enum GameState {LOBBY, RUNNING, STOPPED}

public class GameData {
    public String name;
    public GameType gameType;
    public boolean arenaSaved, enabled;
    public HashMap<String, Player> gamePlayers;
    public HashMap<String, PlayerGameData> gamePlayerObjects;
    public GameState gameState;

    public BlockVector3 startPos1, startPos2, pos1, pos2;
    public Location lobby, spectatorLoc, exitLoc;
    public HashMap<String, Location> teamStartPositions;
    public ArrayList<Location> checkpoints;

    public int timerId, timerCount, playersAlive;
    public HashMap<String, Integer> teamScores;

    public HashMap<String, List<Player>> teamPlayerList;
    public HashMap<String, int[]> playerScores;
    public HashMap<String, int[]> playerOldScores;
    public HashMap<String, Integer> playerLives;
    public HashMap<String, Integer> teamLives;

    public WinCondition winCondition;
    public DeathCondition deathCondition;

    public File gameFolder;

    public GameData(String name, GameType type) {
        this.gameType = type;
        this.name = name;
        this.arenaSaved = false;
        this.enabled = false;
        this.gamePlayers = new HashMap<>();
        this.gamePlayerObjects = new HashMap<>();
        this.gameState = GameState.STOPPED;

        this.startPos1 = null;
        this.startPos2 = null;
        this.pos1 = null;
        this.pos2 = null;
        this.lobby = null;
        this.spectatorLoc = null;
        this.exitLoc = null;
        this.teamStartPositions = null;
        this.checkpoints = null;

        this.timerId = -1;
        this.timerCount = -1;
        this.playersAlive = -1;
        this.teamScores = null;

        this.teamPlayerList = null;
        this.playerScores = null;
        this.playerOldScores = null;
        this.playerLives = null;
        this.teamLives = null;

        this.winCondition = WinCondition.NONE;
        this.deathCondition = DeathCondition.NONE;

        this.gameFolder = new File(Util.getSubPath(MikeyMinigames.data.gamesFolder, name));
        if (!this.gameFolder.exists()) this.gameFolder.mkdir();
    }

    // TODO: javadoc
    public static String gameTypeToString(GameType type) {
        switch (type) {
            case GROUP:
                return "group";
            case MULTIPLAYER:
                return "multiplayer";
            case TEAM:
                return "team";
            case SINGLEPLAYER:
                return "singleplayer";
        }
        return null;
    }

    public static GameType stringToGameType(String s) {
        switch (s) {
            case "group":
                return GameType.GROUP;
            case "multiplayer":
                return GameType.MULTIPLAYER;
            case "team":
                return GameType.TEAM;
            case "singleplayer":
                return GameType.SINGLEPLAYER;
        }
        return null;
    }

    public static String winConditionToString(WinCondition w) {
        switch (w) {
            case NONE:
                return "none";
            case LAST_ALIVE:
                return "last_alive";
            case LAST_TEAM_ALIVE:
                return "last_team_alive";
            case HIGHEST_SCORE:
                return "highest_score";
            case TIMER:
                return "timer";
            case ENDLESS:
                return "endless";
            case LAST_CHECKPOINT:
                return "last_checkpoint";
            case LAST_ROUND:
                return "last_round";
        }
        return null;
    }

    public static WinCondition stringToWinCondition(String s) {
        switch (s) {
            case "none":
                return WinCondition.NONE;
            case "last_alive":
                return WinCondition.LAST_ALIVE;
            case "last_team_alive":
                return WinCondition.LAST_TEAM_ALIVE;
            case "highest_score":
                return WinCondition.HIGHEST_SCORE;
            case "timer":
                return WinCondition.TIMER;
            case "endless":
                return WinCondition.ENDLESS;
            case "last_checkpoint":
                return WinCondition.LAST_CHECKPOINT;
            case "last_round":
                return WinCondition.LAST_ROUND;
        }
        return null;
    }

    public static String deathConditionToString(DeathCondition d) {
        switch (d) {
            case NONE:
                return "none";
            case FALLING:
                return "falling";
            case HEALTH:
                return "health";
        }
        return null;
    }

    public static DeathCondition stringToDeathCondition(String s) {
        switch (s) {
            case "none":
                return DeathCondition.NONE;
            case "falling":
                return DeathCondition.FALLING;
            case "health":
                return DeathCondition.HEALTH;
        }
        return null;
    }

    public static String gameStateToString(GameState s) {
        switch (s) {
            case LOBBY:
                return "lobby";
            case RUNNING:
                return "running";
            case STOPPED:
                return "stopped";
        }
        return null;
    }
}
