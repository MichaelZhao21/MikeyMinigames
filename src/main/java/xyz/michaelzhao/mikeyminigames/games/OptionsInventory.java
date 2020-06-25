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
        inventory = Bukkit.createInventory(null, 9, "Pick the Game Type");
        addItems();
    }

    public void addItems() {
        // Get correct size of inventory
        ItemStack[] items = inventory.getContents();

        // Add 3 game types
        items[2] = Util.createInventoryItem(Material.DIAMOND_SHOVEL, 1, "Spleef",
                "Dig to win!",
                "Break blocks underneath other players",
                "to knock them into the void");
        items[4] = Util.createInventoryItem(Material.RED_BED, 1, "Bedwars",
                "Defend your bed and destroy",
                "the beds of other teams to win!",
                "High-intensity 4-team battle");
        items[6] = Util.createInventoryItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, "Parkour",
                "Jump from block to block to",
                "reach the end!",
                "Checkpoints are included but some",
                "jumps might make you rage :DDD");
        inventory.setContents(items);
    }

    @EventHandler
    public void openInventory(InventoryClickEvent e) {
        if (e.getInventory() != inventory) return;
        if (e.getCurrentItem() == null) return;

        e.setCancelled(true);

        player.closeInventory();
        HandlerList.unregisterAll(MikeyMinigames.data.optionsInventoryPlayerMap.get(player));

        HashMap<Material, GameType> materialTypeMap = new HashMap<>();
        materialTypeMap.put(Material.DIAMOND_SHOVEL, GameType.SPLEEF);
        materialTypeMap.put(Material.RED_BED, GameType.BEDWARS);
        materialTypeMap.put(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, GameType.PARKOUR);

        GameSetup.newGameCallback(gameName, materialTypeMap.get(e.getCurrentItem().getType()), player);
    }

    @EventHandler
    public void closeInventory(InventoryCloseEvent e) {
        if (e.getInventory() != inventory) return;
        if (init) HandlerList.unregisterAll(MikeyMinigames.data.optionsInventoryPlayerMap.get(player));
    }
}
