package xyz.michaelzhao.mikeyminigames;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.michaelzhao.mikeyminigames.games.GameData;
import xyz.michaelzhao.mikeyminigames.games.OptionsInventory;
import xyz.michaelzhao.mikeyminigames.games.ToolInventory;

import java.io.File;
import java.util.HashMap;

public class Data {
    public World currWorld;
    public HashMap<String, GameData> gameData;
    public ToolInventory toolInventory;
    public File gamesFolder;
    public HashMap<Player, String> playersInGameList;
    public HashMap<Player, OptionsInventory> optionsInventoryPlayerMap;
    public HashMap<BlockVector3, String> startingPlates;

    public Data(World currWorld) {
        this.currWorld = currWorld;
        this.gameData = new HashMap<>();
        this.gamesFolder = new File(MikeyMinigames.instance.getDataFolder().getPath() + System.getProperty("file.separator") + "games");
        if (!this.gamesFolder.exists()) this.gamesFolder.mkdir();
        this.playersInGameList = new HashMap<>();
        this.optionsInventoryPlayerMap = new HashMap<>();
        this.startingPlates = new HashMap<>();
    }
}