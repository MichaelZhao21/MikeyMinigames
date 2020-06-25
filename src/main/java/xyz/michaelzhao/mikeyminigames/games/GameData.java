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

enum GameType {SPLEEF, BEDWARS, PARKOUR}

enum GameState {LOBBY, RUNNING, STOPPED}

public class GameData {
    public String name;
    public GameType gameType;
    public boolean arenaSaved, enabled;
    public HashMap<String, Player> gamePlayers;
    public HashMap<String, PlayerGameData> gamePlayerObjects;
    public GameState gameState;

    public BlockVector3 startPos1, startPos2, pos1, pos2;
    public Location lobby, spectatorLoc, exitLoc, startLoc;
    public HashMap<String, Location> teamStartPositions;
    public ArrayList<Location> checkpoints;
    public GameTimer timer;

    public HashMap<String, Integer> teamScores;
    public HashMap<String, Integer> teamLives;
    public HashMap<String, List<Player>> teamPlayerList;

    public HashMap<String, long[]> playerScores;
    public HashMap<String, long[]> playerOldScores;
    public HashMap<String, Integer> playerLives;
    public HashMap<String, long[]> playerTimes;
    public int playersAlive;

    public File gameFolder;
    public File playerFolder;

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
        this.startLoc = null;
        this.teamStartPositions = null;
        this.checkpoints = null;
        this.timer = null;

        this.teamScores = null;
        this.teamLives = null;
        this.teamPlayerList = null;

        this.playerScores = null;
        this.playerOldScores = null;
        this.playerLives = null;
        this.playerTimes = null;
        this.playersAlive = -1;

        this.gameFolder = new File(Util.getSubPath(MikeyMinigames.data.gamesFolder, name));
        if (!this.gameFolder.exists()) this.gameFolder.mkdir();
    }

    public static String gameTypeToString(GameType type) {
        switch (type) {
            case SPLEEF:
                return "spleef";
            case BEDWARS:
                return "bedwars";
            case PARKOUR:
                return "parkour";
        }
        return null;
    }

    public static GameType stringToGameType(String s) {
        switch (s) {
            case "spleef":
                return GameType.SPLEEF;
            case "bedwars":
                return GameType.BEDWARS;
            case "parkour":
                return GameType.PARKOUR;
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
