package xyz.michaelzhao.mikeyminigames.games;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

enum ToolMode {NONE, LOBBY, ARENA, EXIT, SPECTATOR, SPAWN_PLATFORM, SPAWN_POSITION}

public class ToolInventory implements Listener {

    public Inventory inventory;
    public ToolMode toolMode;
    public String toolGame;

    public ToolInventory(String gameName) {
        this.toolGame = gameName;
        this.toolMode = ToolMode.NONE;
        this.inventory = Bukkit.createInventory(null, 18, this.toolGame);
        createInventory();
    }

    public void createInventory() {
        GameData data = Util.getData(toolGame);
        ItemStack[] newInv = new ItemStack[18];

        if (data.lobby != null) newInv[0] = Util.createInventoryItem(Material.RED_BED, 1,
                "Set Lobby", "Click to set lobby position to current position");
        if (data.startPos1 != null && data.startPos2 == null) newInv[1] = Util.createInventoryItem(Material.BRICK, 1,
                "Set Spawn Position", "Click to set spawn position to current position");
        if (data.startPos1 != null && data.startPos2 != null) newInv[2] = Util.createInventoryItem(Material.BRICK_SLAB, 1,
                "Set Spawn Corners", "Left and right click to set the corners of the spawn platform");
        if (data.teamStartPositions != null) newInv[3] = Util.createInventoryItem(Material.RED_WOOL, 1,
                "Set Team Spawn Positions", "Use /games team spawn <Game Name> <Team Name>", "to create a team & set their spawn positions", "/games team remove <Team Name> removes a team");
        if (data.spectatorLoc != null) newInv[4] = Util.createInventoryItem(Material.DROPPER, 1,
                "Set Spectator Spawn Location", "Click to set spectator spawn location to current location");
        if (data.pos1 != null) newInv[5] = Util.createInventoryItem(Material.QUARTZ_BLOCK, 1,
                "Set Arena Corners", "Left and right click to set the corners of the arena", "This will be saved and then loaded after each game");
        if (data.checkpoints != null) newInv[6] = Util.createInventoryItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 1,
                "Set Checkpoints", "To create a checkpoint,", "stand on a light weighted pressure plate (gold)", "and type /games checkpoint add <Game Name>", "to add a checkpoint in the direction you're facing");
        newInv[7] = Util.createInventoryItem(Material.BARRIER, 1,
                "Set Exit Position", "Click to set the game exit position");

        inventory.setContents(newInv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Check to see if this inventory was clicked
        if (e.getInventory() == inventory) {

            // Check if player clicked on smth
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null) return;

            // Don't let player pick up block and close the player's inventory
            e.setCancelled(true);

            // Switch based on the item clicked
            switch (clickedItem.getType()) {
                case RED_BED:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Click to set the lobby position");
                    toolMode = ToolMode.LOBBY;
                    break;
                case QUARTZ_BLOCK:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Left and Right click to set the corners of the game arena");
                    toolMode = ToolMode.ARENA;
                    break;
                case BARRIER:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Click to set the exit position");
                    toolMode = ToolMode.EXIT;
                    break;
                case DROPPER:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Click to set the spectator spawn location");
                    toolMode = ToolMode.SPECTATOR;
                    break;
                case BRICK:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Click to set the spawn position");
                    toolMode = ToolMode.SPAWN_POSITION;
                    break;
                case BRICK_SLAB:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Left and Right click to set the corners of the game spawn platform");
                    toolMode = ToolMode.SPAWN_PLATFORM;
                    break;
                case RED_WOOL:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Use /games team spawn <Game Name> <Team Name> to create a team & set their spawn positions");
                    toolMode = ToolMode.NONE;
                case LIGHT_WEIGHTED_PRESSURE_PLATE:
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "To create a checkpoint, stand on a light weighted pressure plate (gold) and type '/games checkpoint add <Game Name>' to add a checkpoint in the direction you're facing");
                    toolMode = ToolMode.NONE;
                default:
                    toolMode = ToolMode.NONE;
                    return;
            }
            e.getWhoClicked().closeInventory();
            HandlerList.unregisterAll(MikeyMinigames.data.toolInventory);
        }
    }

    public static String toolModeToString(ToolMode toolMode) {
        switch (toolMode) {
            case NONE:
                return "ERROR";
            case LOBBY:
                return "Lobby position";
            case ARENA:
                return "Arena corners";
            case EXIT:
                return "Exit location";
            case SPECTATOR:
                return "Spectator location";
            case SPAWN_PLATFORM:
                return "Spawn platform corners";
            case SPAWN_POSITION:
                return "Spawn position";
        }
        return null;
    }
}
