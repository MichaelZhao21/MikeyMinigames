package xyz.michaelzhao.mikeyminigames.games;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class OptionsInventory implements Listener {

    public boolean init;
    public Inventory inventory;
    public String gameName;
    public Player player;

    public OptionsInventory(boolean init, String gameName, Player player) {
        this.init = init;
        this.gameName = gameName;
        this.player = player;
        if (init) inventory = Bukkit.createInventory(null, 9, "Pick the Game Type");
        else inventory = Bukkit.createInventory(null, 18, "Game Options");
        addItems();
    }

    public void addItems() {
        ItemStack[] items = inventory.getContents();
        if (init) {
            items[1] = Util.createInventoryItem(Material.DIAMOND_SWORD, 1, "Multiplayer",
                    "Games played with multiple players",
                    "Free for all, deathmatch style games",
                    "ex: hunger games, spleef, etc");
            items[3] = Util.createInventoryItem(Material.ENDER_CHEST, 1, "Team",
                    "Games played with teams",
                    "Teams compete against each other to win",
                    "ex: bedwars, cops and robbers, etc");
            items[5] = Util.createInventoryItem(Material.BELL, 1, "Group",
                    "Co-op games played in a group",
                    "Players work together for a common goal",
                    "Game ends when a score threshold, time limit,",
                    "or level is met",
                    "ex: zombies, killing the ender dragon, etc");
            items[7] = Util.createInventoryItem(Material.TOTEM_OF_UNDYING, 1, "Singleplayer",
                    "Games played alone!",
                    "Normally games that can be played",
                    "while other players are also playing it",
                    "ex: parkour, target practice, etc");
        }
        else {
            addOptions(items);
        }
        inventory.setContents(items);
    }

    public void addOptions(ItemStack[] items) {
        GameData data = Util.getData(gameName);
        items[0] = Util.getToggle(Material.RED_BED, "Lobby", data.lobby != null);
        items[1] = Util.getToggle(Material.BRICKS, "Starting Location", data.startPos1 != null && data.startPos2 == null);
        items[2] = Util.getToggle(Material.BRICK_SLAB, "Starting Platform", data.startPos1 != null && data.startPos2 != null);
        items[3] = Util.getToggle(Material.RED_WOOL, "Team Starting Locations", data.teamStartPositions != null);
        items[4] = Util.getToggle(Material.DROPPER, "Spectators", data.spectatorLoc != null);
        items[5] = Util.getToggle(Material.QUARTZ, "Regen Arena", data.pos1 != null && data.pos2 != null);
        items[6] = Util.getToggle(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, "Checkpoints", data.checkpoints != null);
        items[7] = Util.getToggle(Material.WOODEN_HOE, "Player Scores", data.playerScores != null);
        items[8] = Util.getToggle(Material.WOODEN_AXE, "Player Old Scores", data.playerOldScores != null);
        items[9] = Util.getToggle(Material.DIAMOND_PICKAXE, "Team Scores", data.teamScores != null);
        items[10] = Util.getToggle(Material.POTION, "Player Lives", data.playerLives != null);
        items[11] = Util.getToggle(Material.DRAGON_BREATH, ChatColor.WHITE + "Team Lives", data.teamLives != null);
        items[12] = Util.getToggle(Material.CLOCK, "Timer", data.timerId != -1);
        items[13] = Util.createInventoryItem(Material.DARK_OAK_DOOR, 1, "Levels", "WIP");
    }

    @EventHandler
    public void openInventory(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true);

        if (init) {
            player.closeInventory();
            HandlerList.unregisterAll(MikeyMinigames.data.optionsInventoryPlayerMap.get(player));

            HashMap<Material, GameType> materialTypeMap = new HashMap<>();
            materialTypeMap.put(Material.DIAMOND_SWORD, GameType.MULTIPLAYER);
            materialTypeMap.put(Material.ENDER_CHEST, GameType.TEAM);
            materialTypeMap.put(Material.BELL, GameType.GROUP);
            materialTypeMap.put(Material.TOTEM_OF_UNDYING, GameType.SINGLEPLAYER);

            GameSetup.newGameCallback(gameName, materialTypeMap.get(e.getCurrentItem().getType()), player);
        }
        else {
            GameData data = Util.getData(gameName);
            switch (e.getCurrentItem().getType()) {
                case RED_BED:
                    if (data.lobby == null) data.lobby = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                    else data.lobby = null;
                    break;
                case BRICKS:
                    if (data.startPos1 != null && data.startPos2 != null) {
                        data.startPos1 = BlockVector3.at(0, 0, 0);
                        data.startPos2 = null;
                    }
                    else if (data.startPos1 == null) data.startPos1 = BlockVector3.at(0, 0, 0);
                    else data.startPos1 = null;
                    break;
                case BRICK_SLAB:
                    if (data.startPos2 == null) {
                        data.startPos1 = BlockVector3.at(0, 0, 0);
                        data.startPos2 = BlockVector3.at(0, 0, 0);
                    }
                    else {
                        data.startPos1 = null;
                        data.startPos2 = null;
                    }
                    break;
                case RED_WOOL:
                    if (data.teamStartPositions == null) data.teamStartPositions = new HashMap<>();
                    else data.teamStartPositions = null;
                    break;
                case DROPPER:
                    if (data.spectatorLoc == null) data.spectatorLoc = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                    else data.spectatorLoc = null;
                    break;
                case QUARTZ:
                    if (data.pos1 == null) {
                        data.pos1 = BlockVector3.at(0, 0, 0);
                        data.pos2 = BlockVector3.at(0, 0, 0);
                    }
                    else {
                        data.pos1 = null;
                        data.pos2 = null;
                    }
                    break;
                case LIGHT_WEIGHTED_PRESSURE_PLATE:
                    if (data.checkpoints == null) data.checkpoints = new ArrayList<>();
                    else data.checkpoints = null;
                    break;
                case WOODEN_HOE:
                    if (data.playerScores == null) data.playerScores = new HashMap<>();
                    else data.playerScores = null;
                    break;
                case WOODEN_AXE:
                    if (data.playerOldScores == null) data.playerOldScores = new HashMap<>();
                    else data.playerOldScores = null;
                    break;
                case DIAMOND_PICKAXE:
                    if (data.teamScores == null) data.teamScores = new HashMap<>();
                    else data.teamScores = null;
                    break;
                case POTION:
                    if (data.playerLives == null) data.playerLives = new HashMap<>();
                    else data.playerLives = null;
                    break;
                case DRAGON_BREATH:
                    if (data.teamLives == null) data.teamLives = new HashMap<>();
                    else data.teamLives = null;
                    break;
                case CLOCK:
                    if (data.timerId == -1) {
                        data.timerId = 0;
                        data.timerCount = 0;
                    }
                    else {
                        data.timerId = -1;
                        data.timerCount = -1;
                    }
                    break;
                default:
                    return;
            }
            ItemStack[] items = inventory.getContents();
            addOptions(items);
            inventory.setContents(items);
        }
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent e) {
        if (e.getInventory() != inventory) return;
        if (init) HandlerList.unregisterAll(MikeyMinigames.data.optionsInventoryPlayerMap.get(player));
    }
}
