package xyz.michaelzhao.mikeyminigames.games;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

public class GameListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = event.getItem();

        // Right or left click on the minigame tool
        if (item != null &&
                item.getType() == Material.BLAZE_ROD &&
                item.getItemMeta() != null &&
                item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Minigame Tool")) {

            // Sneak click to open inventory
            if (player.getPose() == Pose.SNEAKING && (action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_BLOCK))) {
                // Create tool inventory class with game name
                MikeyMinigames.data.toolInventory = new ToolInventory(item.getItemMeta().getLore().get(0));
                player.openInventory(MikeyMinigames.data.toolInventory.inventory);

                // Register event listener
                MikeyMinigames.instance.getServer().getPluginManager().registerEvents(MikeyMinigames.data.toolInventory, MikeyMinigames.instance);
                return;
            }

            // Get tool mode
            ToolMode mode = MikeyMinigames.data.toolInventory.toolMode;

            // Make sure tool has a mode
            if (mode == ToolMode.NONE) return;

            // Run corners or position setting
            if (mode == ToolMode.ARENA || mode == ToolMode.SPAWN_PLATFORM)
                GameSetup.setCorners(player, action, event);
            else
                GameSetup.setPos(MikeyMinigames.data.toolInventory.toolMode, action, player);
            return;
        }

        // Clicks on item for parkour
        if (item != null &&
                MikeyMinigames.data.playersInGameList.containsKey(player) &&
                Util.getData(MikeyMinigames.data.playersInGameList.get(player)).gameType == GameType.PARKOUR) {

            // TRAP!
            event.setCancelled(true);

            // Do thing based on item type
            switch (item.getType()) {
                case LIGHT_WEIGHTED_PRESSURE_PLATE:
                    GameData data = Util.getData(MikeyMinigames.data.playersInGameList.get(player));
                    player.teleport(data.checkpoints.get(data.playerLives.get(player.getName())));
                    player.setVelocity(new Vector(0, 0, 0));
                    break;
                case OAK_DOOR:
                    String gameName = Util.getData(MikeyMinigames.data.playersInGameList.get(player)).name;
                    GameEngine.removeFromGame(player);
                    GameEngine.joinGame(player, gameName, false);
                    player.setVelocity(new Vector(0, 0, 0));
                    break;
                case BARRIER:
                    GameEngine.quit(player);
                    break;
            }
            return;
        }

        // Step on pressure plate for parkour checkpoint
        if (action == Action.PHYSICAL &&
                event.getClickedBlock() != null &&
                event.getClickedBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {

            // If game already started
            if (MikeyMinigames.data.playersInGameList.containsKey(player) &&
                    Util.getData(MikeyMinigames.data.playersInGameList.get(player)).gameType == GameType.PARKOUR) {
                // Get game data
                GameData data = Util.getData(MikeyMinigames.data.playersInGameList.get(player));

                // Get checkpoint #
                int cpNum = Util.findLocationInArrList(data.checkpoints, event.getClickedBlock().getLocation());

                // Get curr cp #
                int currNum = data.playerLives.get(player.getName());

                // Return if the cp number is lower than the current cp number
                if (cpNum <= currNum) {
                    return;
                }

                // Check for cheating
                if (cpNum != currNum + 1) {
                    player.sendMessage(ChatColor.RED + "You're skipping checkpoints! No cheating~ goodbye!");
                    GameEngine.removeFromGame(player);
                    return;
                }

                // Increment the checkpoint number
                data.playerLives.put(player.getName(), ++currNum);

                // Get the time and the score
                long[] times = data.playerTimes.get(player.getName());
                long[] scores = data.playerScores.get(player.getName());

                // Compute the current score
                times[currNum] = System.currentTimeMillis();
                scores[currNum] = times[currNum] - times[currNum - 1];

                // Check for end game
                if (currNum == times.length - 1) {
                    GameEngine.endGame(data, player);
                }

                // Print to player
                player.sendMessage(ChatColor.GOLD + "You have reached checkpoint " + currNum + "!");
                player.sendMessage(ChatColor.GREEN + "Section " + (currNum - 1) + " time: " + Util.formatTime(scores[currNum]));
                return;
            }

            // Check for start and start game
            if (MikeyMinigames.data.startingPlates.containsKey((Util.locationToBlockVector3(event.getClickedBlock().getLocation())))) {
                GameEngine.joinGame(player, MikeyMinigames.data.startingPlates.get((Util.locationToBlockVector3(event.getClickedBlock().getLocation()))), true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.DIAMOND_PICKAXE &&
                item.getItemMeta() != null &&
                item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "S P O O N")) {
            e.setDropItems(false);
        }
    }

    @EventHandler
    public void onPlayerFallEvent(PlayerMoveEvent e) {
        if (e.getTo() != null && e.getTo().getY() < 0 &&
                MikeyMinigames.data.playersInGameList.containsKey(e.getPlayer())) {
            GameData data = Util.getData(MikeyMinigames.data.playersInGameList.get(e.getPlayer()));
            if (data.gameType == GameType.SPLEEF)
                GameEngine.playerDeath(e.getPlayer(), MikeyMinigames.data.playersInGameList.get(e.getPlayer()));
            else if (data.gameType == GameType.PARKOUR) {
                e.getPlayer().teleport(data.checkpoints.get(data.playerLives.get(e.getPlayer().getName())));
                e.getPlayer().setVelocity(new Vector(0, 0, 0));
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (MikeyMinigames.data.playersInGameList.containsKey(e.getPlayer()) &&
                Util.getData(MikeyMinigames.data.playersInGameList.get(e.getPlayer())).gameType == GameType.PARKOUR)
            e.setCancelled(true);
    }

    // TODO check for theow iyem
}
