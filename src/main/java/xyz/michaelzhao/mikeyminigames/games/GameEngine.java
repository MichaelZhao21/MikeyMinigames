package xyz.michaelzhao.mikeyminigames.games;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import xyz.michaelzhao.mikeyminigames.FunCommands;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

public class GameEngine {
    /**
     * Changes the enabled attribute after checking for setup conditions
     *
     * @param sender the sender that issued the command
     * @param args   command arguments
     */
    public static void enableGame(CommandSender sender, String[] args) {
        // Command checking
        if (Util.isArgsIncorrectLength(args, 2, "games enable <GameName>", sender)) return;
        if (Util.isInvalidGame(args[1], sender)) return;

        // Define objects for comparing to the BlockVector3 and Location objects
        BlockVector3 blockNotSet = BlockVector3.at(0, 0, 0);

        // Get the current game as the base class
        GameData data = Util.getData(args[1]);

        // Make variable to check for errors
        boolean noError = true;

        // Check all for errors
        if (enableError(Util.isLocationNotSet(data.exitLoc), "Exit location not set", sender))
            noError = false;
        if (data.lobby != null)
            if (enableError(Util.isLocationNotSet(data.lobby), "Lobby not set", sender))
                noError = false;
        if (data.pos1 != null)
            if (enableError(data.pos1.equals(blockNotSet) || data.pos2.equals(blockNotSet), "Arena bounds not set", sender))
                noError = false;
        if (data.startLoc != null)
            if (enableError(Util.isLocationNotSet(data.startLoc), "Start location not set", sender))
                noError = false;
        if (data.startPos1 != null && data.startPos2 != null)
            if (enableError(data.startPos1.equals(blockNotSet) || data.startPos2.equals(blockNotSet), "Starting platform not set", sender))
                noError = false;
        if (data.spectatorLoc != null)
            if (enableError(Util.isLocationNotSet(data.spectatorLoc), "Spectator spawn position not set", sender))
                noError = false;
        if (data.checkpoints != null)
            if (enableError(data.checkpoints.size() < 2, "You must set at least 2 checkpoints (start and end)", sender))
                noError = false;

        // Enable if there were no errors
        if (noError) {
            Util.getData(args[1]).enabled = true;
            sender.sendMessage(ChatColor.GOLD + args[1] + " enabled!");

            // Add starting checkpoint to list
            MikeyMinigames.data.startingPlates.put(Util.locationToBlockVector3(data.checkpoints.get(0)), data.name);
        }
    }

    public static boolean enableError(boolean condition, String errorMessage, CommandSender sender) {
        if (condition) sender.sendMessage(errorMessage);
        return condition;
    }

    /**
     * Disables the game after checking to make sure the game isn't currently disabled
     *
     * @param player the player that issued the command
     * @param args   command arguments
     */
    public static void disableGame(Player player, String[] args) {
        // Check args
        if (Util.isArgsIncorrectLength(args, 2, "games disable <Game Name>", player)) return;
        if (Util.isInvalidGame(args[1], player)) return;

        // Disable the game and send player message
        Util.getData(args[1]).enabled = false;
        player.sendMessage(ChatColor.GOLD + args[1] + " disabled");
    }

    /**
     * Prints out info for the data object
     *
     * @param player the player that issued the command
     * @param args   command arguments
     */
    public static void info(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 2, "games info <Game Name>", player)) return;
        if (Util.isInvalidGame(args[1], player)) return;

        // Get game data object
        GameData data = Util.getData(args[1]);

        // Print out info
        player.sendMessage("-----------------------------------");
        player.sendMessage(ChatColor.GOLD + "Name: " + data.name);
        if (data.lobby != null) player.sendMessage(Util.locationToString("Lobby Position", data.lobby));
        if (data.startPos1 != null) player.sendMessage(Util.blockVector3ToString("Start Pos 1", data.startPos1));
        if (data.startPos2 != null) player.sendMessage(Util.blockVector3ToString("Spawn Pos 2", data.startPos2));
        if (data.startLoc != null) player.sendMessage(Util.locationToString("Start Location", data.startLoc));
        if (data.teamStartPositions != null)
            player.sendMessage(Util.stringLocationHashToString("Team Start Positions: ", data.teamStartPositions));
        if (data.spectatorLoc != null)
            player.sendMessage(Util.locationToString("Spectator Location", data.spectatorLoc));
        if (data.pos1 != null) player.sendMessage(Util.blockVector3ToString("Arena Pos 1", data.pos1));
        if (data.pos2 != null) player.sendMessage(Util.blockVector3ToString("Arena Pos 2", data.pos2));
        if (data.checkpoints != null)
            player.sendMessage(Util.locationArrayListToString("Checkpoint Locations", data.checkpoints));
        if (data.playerOldScores != null)
            player.sendMessage(Util.stringLongArrayHashToString("Player Old Scores", data.playerOldScores));
        player.sendMessage("Game State: " + GameData.gameStateToString(data.gameState));
        player.sendMessage("Enabled: " + data.enabled);
        player.sendMessage(Util.locationToString("Exit Location", data.exitLoc));
        player.sendMessage("-----------------------------------");
    }

    public static void giveKitCommand(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 2, "games kit <spleef | bedwars | parkour>", player)) return;

        // Check to make sure game type is valid
        GameType type = GameData.stringToGameType(args[1]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + args[1] + " is not a valid kit name");
            return;
        }

        // Give player kit
        giveKit(type, player);
    }

    /**
     * Gives the kit specified to the player
     *
     * @param type   the type of kit to give
     * @param player the player that issued the command
     */
    public static void giveKit(GameType type, Player player) {
        // Give kit based on type
        switch (type) {
            case SPLEEF:
                // Create the pickaxe and add metadata, then give to player
                ItemStack pick = Util.createInventoryItem(Material.DIAMOND_PICKAXE, 1, ChatColor.GREEN + "S P O O N", "digdigdig");
                ItemMeta meta = pick.getItemMeta();
                meta.addEnchant(Enchantment.DIG_SPEED, 100, true);
                meta.addEnchant(Enchantment.DURABILITY, 100, true);
                meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
                pick.setItemMeta(meta);
                player.getInventory().addItem(pick);

                // Add potion effects to player
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 10, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 10, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 10, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 0, false, false));
                break;
            case BEDWARS:
                // TODO: Add stuff but i think its nothing lol
                break;
            case PARKOUR:
                ItemStack[] items = player.getInventory().getContents();
                items[3] = Util.createInventoryItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 1, ChatColor.GREEN + "Last Checkpoint", "Right click to go to the last checkpoint");
                items[4] = Util.createInventoryItem(Material.OAK_DOOR, 1, ChatColor.YELLOW + "Reset", "Right click to reset parkour and go to start");
                items[5] = Util.createInventoryItem(Material.BARRIER, 1, ChatColor.DARK_RED + "Exit", "Right click to exit parkour");
                player.getInventory().setContents(items);
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 10, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000000, 10, false, false));
                break;
        }
    }

    /**
     * Joins a game lobby
     *
     * @param player the player who issued the command
     * @param args   command arguments
     */
    public static void joinGameCommand(Player player, String[] args) {
        // Check commands
        if (Util.isArgsIncorrectLength(args, 2, "games join <GameName>", player)) return;
        if (Util.isInvalidGame(args[1], player)) return;

        // Check if player is already in game
        if (MikeyMinigames.data.playersInGameList.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "Already in game " + MikeyMinigames.data.playersInGameList.get(player));
            return;
        }

        joinGame(player, args[1]);
    }

    public static void joinGame(Player player, String gameName) {

        // Get game data object
        GameData data = Util.getData(gameName);

        // Add player to hashmap of players
        data.gamePlayers.put(player.getName(), player);

        // Add player data to hashmap
        data.gamePlayerObjects.put(player.getName(), new PlayerGameData(player));

        // Make sure the game is enabled
        if (!data.enabled) {
            player.sendMessage(ChatColor.RED + "Game not enabled!");
            return;
        }

        // Add player to list of players in a game
        MikeyMinigames.data.playersInGameList.put(player, data.name);

        // Check for stopped state
        if (data.gameState == GameState.RUNNING && data.gameType != GameType.PARKOUR) { // TODO: Add join as spectator
            player.sendMessage(ChatColor.RED + "Game is currently playing");
            return;
        }

        // Send player joined message
        player.sendMessage(ChatColor.AQUA + "Joined " + ChatColor.GOLD + gameName);

        // Clear inventory and prepare them for the game
        player.getInventory().clear();
        player.setHealth(20.0);
        player.setGameMode(GameMode.ADVENTURE);
        giveKit(data.gameType, player);

        // Teleport player to start
        if (!(data.gameType == GameType.PARKOUR && Util.compLocations(player.getLocation(), data.checkpoints.get(0))))
            player.teleport(data.gameType == GameType.PARKOUR ? data.checkpoints.get(0) : data.lobby);

        // If the game hasn't begun, start it
        if (data.gameState == GameState.STOPPED && data.lobby != null)
            startLobby(data);

        // Parkour specific things
        if (data.gameType == GameType.PARKOUR) {
            // Create time/scores arrays
            long[] times = new long[data.checkpoints.size()];
            times[0] = 0;
            long[] scores = new long[times.length];
            scores[0] = System.currentTimeMillis();

            // Add the arrays to the maps
            data.playerTimes.put(player.getName(), times);
            data.playerScores.put(player.getName(), scores);
            data.playerLives.put(player.getName(), 0);
            data.gameState = GameState.RUNNING;
        }
    }

    /**
     * Start the lobby
     *
     * @param data the game data
     */
    public static void startLobby(GameData data) {
        data.timer = new GameTimer(data.name, true, 30, "start", null, TimerDisplay.XP);
        data.gameState = GameState.LOBBY;
    }

    /**
     * Command to autostart the game
     *
     * @param player the player that issued the command
     * @param args   command arguments
     */
    public static void startCall(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 2, "games start <GameName>", player)) return;
        if (Util.isInvalidGame(args[1], player)) return;

        // Get game data object
        GameData data = Util.getData(args[1]);

        // Check if the game is in the lobby state
        if (data.gameState != GameState.LOBBY) {
            player.sendMessage(ChatColor.RED + args[1] + " is not in the lobby!");
            return;
        }

        // Cancel timer and start game
        MikeyMinigames.instance.getServer().getScheduler().cancelTask(data.timer.id);
        start(args[1]);
    }

    /**
     * Start the game
     *
     * @param gameName the name of the game
     */
    public static void start(String gameName) {
        // Get game data object
        GameData data = Util.getData(gameName);

        // TODO: Add cases for game types

        // Set the state to running
        data.gameState = GameState.RUNNING;

        // Create the game stopwatch
        data.timer = new GameTimer(gameName, false, 0, null, "endgame", TimerDisplay.XP);

        // Set the playersAlive to the number of players in the game
        data.playersAlive = data.gamePlayers.size();

        // Prep and teleport each player to the game arena
        for (Player player : data.gamePlayers.values()) {
            data.gamePlayerObjects.get(player.getName()).state = PlayerState.GAME;
            player.setGameMode(GameMode.SURVIVAL);
            player.setLevel(0);
            player.teleport(randomSpawn(data.startPos1, data.startPos2));
        }
    }

    /**
     * Generates a random starting point from 2 corners
     *
     * @param start lowest-coordinate-valued corner
     * @param end   highest-coordinate-valued corner
     * @return the Location object representing spawn point
     */
    public static Location randomSpawn(BlockVector3 start, BlockVector3 end) { //TODO: Add facing center
        double x = Math.random() * (end.getX() - start.getX()) + start.getX();
        double z = Math.random() * (end.getZ() - start.getZ()) + start.getZ();
        return new Location(MikeyMinigames.data.currWorld, x, start.getY(), z);
    }

    /**
     * Player leaves game
     *
     * @param player the player that issued the command
     */
    public static void quit(Player player) {
        // Check to make sure player is in a game
        if (!MikeyMinigames.data.playersInGameList.containsKey(player)) {
            player.sendMessage(ChatColor.RED + "Not in a game");
            return;
        }

        // Remove player from game and send message
        player.sendMessage(ChatColor.AQUA + "Left game " + ChatColor.GOLD + Util.getData(MikeyMinigames.data.playersInGameList.get(player)).name);
        removeFromGame(player);
    }

    /**
     * Runs commands to remove the player from the game
     *
     * @param player the player that issued the command
     */
    public static void removeFromGame(Player player) {
        // Get data object
        GameData data = Util.getData(MikeyMinigames.data.playersInGameList.get(player));

        // Get the player data object
        PlayerGameData pDat = data.gamePlayerObjects.get(player.getName());

        // Remove players from the list of players in the game and general list TODO fix this desc
        data.gamePlayers.remove(player.getName());
        data.gamePlayerObjects.remove(player.getName());
        MikeyMinigames.data.playersInGameList.remove(player);

        // Restore the player's items
        ItemStack[] items = pDat.oldInventory;
        player.getInventory().clear();
        player.getInventory().setContents(items);

        // Remove all potion effects and restore old effects
        for (PotionEffect e : player.getActivePotionEffects())
            player.removePotionEffect(e.getType());
        for (PotionEffect p : pDat.oldPotionEffects)
            player.addPotionEffect(p);

        // Reset gamemode/xp, set velocity to 0, and teleport them to the exit location
        player.setGameMode(pDat.oldMode);
        player.setTotalExperience(pDat.oldExpLvl);
        player.setVelocity(new Vector());
        player.teleport(data.exitLoc);
    }


    /**
     * Checks to see if the game ends based on specific conditions
     *
     * @param data the game data object
     */
    public static void checkForEndGame(GameData data) {
        switch (data.gameType) {
            case SPLEEF:
                // For deathmatches, check to see if only one player is alive
                if (data.playersAlive == 1) {
                    endGame(data, null);
                }
                break;
            case BEDWARS:
                // TODO :D
                break;
            case PARKOUR:

                break;
        }
    }

    public static void endGame(GameData data, Player player) {
        switch (data.gameType) {
            case SPLEEF:
                // Set them as winner and get their name
                Player winner = getWinner(data);
                String winName = winner.getName();

                // Cancel the running timer
                MikeyMinigames.instance.getServer().getScheduler().cancelTask(data.timer.id);

                // Get a list of player names and iterate through them, removing them from the game
                String[] pm = data.gamePlayers.keySet().toArray(new String[0]);
                for (String p : pm) {
                    Player currP = data.gamePlayers.get(p);
                    removeFromGame(currP);

                    // Show all players who won the game
                    currP.sendTitle(String.format("%s won %s!", ChatColor.GOLD + winName, ChatColor.AQUA + data.name), "", 10, 60, 20);
                }

                // Reload destroyed arena if exists
                if (data.pos1 != null) GameSetup.loadArena(data);

                // Set game state to stopped
                data.gameState = GameState.STOPPED;
                break;
            case PARKOUR:
                long[] scores = data.playerScores.get(player.getName());
                player.sendMessage("" + ChatColor.BOLD + ChatColor.GOLD + "YOU FINISHED THE PARKOUR IN " + Util.formatTime(scores[scores.length - 1]));
                // TODO: Save player score and stuff
                GameEngine.removeFromGame(player);
                FunCommands.spawnRandomFirework(player);
        }
    }

    /**
     * Finds the winner from the list of players in a game
     *
     * @param data the game data
     * @return the Player object representing the winner
     */
    public static Player getWinner(GameData data) {
        for (Player p : data.gamePlayers.values())
            if (data.gamePlayerObjects.get(p.getName()).state == PlayerState.GAME)
                return p;
        return null;
    }

    /**
     * Runs when the player dies: Normally health below 0 or y-level below 0
     * Is called on event listener
     *
     * @param player   the player that died
     * @param gameName the name of the game
     */
    public static void playerDeath(Player player, String gameName) {
        // Get game data object
        GameData data = Util.getData(gameName);

        // Check to see if the game is running and the player is in the current game
        if (data.gameState == GameState.RUNNING && data.gamePlayerObjects.get(player.getName()).state == PlayerState.GAME) {
            // Set the player's state to spectator and decrease players alive counter
            data.gamePlayerObjects.get(player.getName()).state = PlayerState.SPECTATOR;
            data.playersAlive--;

            // Tell the player they died and change them to a spectator
            player.sendTitle("You died!", "You lasted " + data.timer.count + " seconds", 10, 60, 20);
            player.setGameMode(GameMode.SPECTATOR);
            player.getInventory().clear();
            player.teleport(data.spectatorLoc);

            // Check to see if that death caused the winning condition
            checkForEndGame(data);

            // Notify all players that the current player died
            for (Player p : data.gamePlayers.values())
                p.sendMessage(ChatColor.AQUA + player.getName() + " died! " + ChatColor.LIGHT_PURPLE + data.playersAlive + " players remaining.");
        }
    }
}
