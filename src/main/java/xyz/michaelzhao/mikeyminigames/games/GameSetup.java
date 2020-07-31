package xyz.michaelzhao.mikeyminigames.games;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.michaelzhao.mikeyminigames.MikeyMinigames;
import xyz.michaelzhao.mikeyminigames.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.UUID;

public class GameSetup {
    /**
     * Creates a new game with a game name and type
     *
     * @param player player that issued the command
     * @param args   command arguments
     */
    public static void newGame(Player player, String[] args) {
        // Check args
        if (Util.isArgsIncorrectLength(args, 2, "games add <Game Name>", player)) return;

        // LOWERCASE WIGHEWUF
        args[1] = args[1].toLowerCase();

        // Check if the game exists
        if (MikeyMinigames.data.gameData.containsKey(args[1])) {
            player.sendMessage(ChatColor.RED + args[1] + " already exists!");
            return;
        }

        // Open options
        OptionsInventory options = new OptionsInventory(true, args[1], player);
        MikeyMinigames.data.optionsInventoryPlayerMap.put(player, options);
        player.openInventory(options.inventory);

        // Register listener
        MikeyMinigames.instance.getServer().getPluginManager().registerEvents(options, MikeyMinigames.instance);
    }

    /**
     * Callback function for game type inventory
     *
     * @param gameName the name of the game
     * @param type     the type of the game
     * @param player   the player that issued the command
     */
    public static void newGameCallback(String gameName, GameType type, Player player) {
        // Get game data object
        GameData data = new GameData(gameName, type);

        // Sets the default values based on type
        setDefaults(data);

        // Add to hashmap and send added message
        MikeyMinigames.data.gameData.put(gameName, data);
        player.sendMessage(ChatColor.GOLD + "Added " + gameName);

        // Saves game
        saveGame(gameName);

        // Creates and opens the main options menu for the player
        giveTool(player, new String[]{"tool", gameName});
    }

    /**
     * Opens the options menu
     *
     * @param gameName the name of the game
     * @param player   the player that issued the command
     */
    public static void optionsMenu(String gameName, Player player) {
        OptionsInventory mainOptions = new OptionsInventory(false, gameName, player);
        MikeyMinigames.data.optionsInventoryPlayerMap.put(player, mainOptions);
        MikeyMinigames.instance.getServer().getPluginManager().registerEvents(mainOptions, MikeyMinigames.instance);
        player.openInventory(mainOptions.inventory);
    }

    public static void setDefaults(GameData data) {
        switch (data.gameType) {
            case SPLEEF:
                data.startPos1 = BlockVector3.at(0, 0, 0);
                data.startPos2 = BlockVector3.at(0, 0, 0);
                data.lobby = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.spectatorLoc = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.exitLoc = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.timer = new GameTimer();
                data.playersAlive = 0;
                data.pos1 = BlockVector3.at(0, 0, 0);
                data.pos2 = BlockVector3.at(0, 0, 0);
                break;
            case BEDWARS:
                data.teamStartPositions = new HashMap<>();
                data.lobby = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.spectatorLoc = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.exitLoc = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.timer = new GameTimer();
                data.teamPlayerList = new HashMap<>();
                data.teamScores = new HashMap<>();
                break;
            case PARKOUR:
                data.exitLoc = new Location(MikeyMinigames.data.currWorld, 0, 0, 0);
                data.checkpoints = new ArrayList<>();
                data.playerScores = new HashMap<>();
                data.playerOldScores = new HashMap<>();
                data.playerLives = new HashMap<>();
                data.playerTimes = new HashMap<>();
                break;
        }
    }

    /**
     * Deletes a game and its file contents
     * @param player player that issued the command
     * @param args   command arguments
     */
    public static void removeGame(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 2, "games remove <Game Name>", player)) return;
        if (Util.isInvalidGame(args[1], player)) return;

        // Get game data
        GameData data = Util.getData(args[1]);

        // Remove the game folder
        Util.deleteDirectory(data.gameFolder);

        // Delete game from hashmap
        MikeyMinigames.data.gameData.remove(args[1]);
    }

    /**
     * Gives the arena selection tool to the player
     *
     * @param player player that issued the command
     * @param args   command arguments
     */
    public static void giveTool(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 2, "games tool <Game Name>", player)) return;
        if (Util.isInvalidGame(args[1], player)) return;

        // Add items to player and send message
        player.getInventory().addItem(Util.createInventoryItem(Material.BLAZE_ROD, 1,
                ChatColor.GOLD + "Minigame Tool",
                args[1]));
        player.sendMessage(ChatColor.AQUA + "Minigame tool - Select corners of the game area (to be saved and regenerated)");
        player.sendMessage(ChatColor.AQUA + "Left click to select pos1 and right click to select pos2");
    }

    /**
     * List out the games currently avaliable
     *
     * @param player player that issued the command
     */
    public static void list(Player player, String[] args) {
        // Check args length
        if (Util.isArgsIncorrectLength(args, 1, "games list", player)) return;

        // List title
        player.sendMessage(ChatColor.AQUA + "List of minigames:");

        // Print out list of games and save count
        int count = 0;
        for (String str : MikeyMinigames.data.gameData.keySet()) {
            count++;
            player.sendMessage(ChatColor.GRAY + Integer.toString(count) + ". " + ChatColor.GREEN + str);
        }

        // Send message if no games
        if (count == 0)
            player.sendMessage(ChatColor.GREEN + "No games avaliable");
    }

    public static void setCorners(Player player, Action action, PlayerInteractEvent event) {
        if (action.equals(Action.RIGHT_CLICK_BLOCK) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            // Get the block clicked
            Block block = event.getClickedBlock();
            if (block == null) return;

            // Get coordinates and store that position
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            // Get game data object
            GameData data = Util.getData(MikeyMinigames.data.toolInventory.toolGame);

            // Store in pos1/pos2 or startPos1/startPos2 based on clicks and tool mode
            // TODO: Encapsulate or make nicer (repeated code!)
            if (action.equals(Action.LEFT_CLICK_BLOCK)) {
                event.setCancelled(true);
                player.sendMessage(String.format("%sPos1 set to: (%d, %d, %d)", ChatColor.LIGHT_PURPLE, x, y, z));
                if (MikeyMinigames.data.toolInventory.toolMode == ToolMode.ARENA)
                    data.pos1 = BlockVector3.at(x, y, z);
                else
                    data.startPos1 = BlockVector3.at(x, y + 1, z);
            } else {
                player.sendMessage(String.format("%sPos2 set to: (%d, %d, %d)", ChatColor.LIGHT_PURPLE, x, y, z));
                if (MikeyMinigames.data.toolInventory.toolMode == ToolMode.ARENA)
                    data.pos2 = BlockVector3.at(x, y, z);
                else
                    data.startPos2 = BlockVector3.at(x, y + 1, z);
            }
        }
    }

    /**
     * Set position with tool click based on player position
     *
     * @param toolMode the mode the tool is in (based on the enum)
     * @param action   the action performed
     * @param player   player that issued the command
     */
    public static void setPos(ToolMode toolMode, Action action, Player player) {
        // Get if the player left clicked
        boolean leftClick = (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR);

        // Get game data object
        GameData data = Util.getData(MikeyMinigames.data.toolInventory.toolGame);

        // Get player location
        Location loc = player.getLocation();
        BlockVector3 pos = BlockVector3.at(loc.getX(), loc.getY(), loc.getZ());
        switch (toolMode) {
            case LOBBY:
                data.lobby = loc;
                break;
            case SPAWN_PLATFORM:
                if (leftClick)
                    data.startPos1 = pos;
                else
                    data.startPos2 = pos;
                break;
            case SPAWN_POSITION:
                data.startLoc = loc;
                break;
            case SPECTATOR:
                data.spectatorLoc = loc;
                break;
            case EXIT:
                data.exitLoc = loc;
                break;
        }
        player.sendMessage(ChatColor.GOLD + ToolInventory.toolModeToString(toolMode) + " set!");
    }

    public static void addCheckpoint(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 3, "games checkpoint add <Game Name>", player)) return;
        if (Util.isInvalidGame(args[2], player)) return;

        // Get game data
        GameData data = Util.getData(args[2]);

        // Check if checkpoints are enabled
        if (data.checkpoints == null) {
            player.sendMessage(ChatColor.RED + "Checkpoints not enabled for " + data.name);
            return;
        }

        // Get block player is standing on
        Block b = MikeyMinigames.data.currWorld.getBlockAt(player.getLocation());

        // Check to make sure it's a light weighted pressure plate
        if (b.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            player.sendMessage(ChatColor.RED + "Stand on a light weighted pressure plate to add a checkpoint");
            return;
        }

        // Add the block location and the player location to the arraylists
        data.checkpoints.add(player.getLocation());

        // Feedback to player
        player.sendMessage(ChatColor.GOLD + "Checkpoint " + (data.checkpoints.size() - 1) + " added!");
        player.sendMessage(ChatColor.LIGHT_PURPLE + Util.locationToString("Checkpoint Location", player.getLocation()));
    }

    public static void teamCommand(Player player, String[] args) {
        // List
        if (args.length == 3 && args[1].equals("list") && !Util.isInvalidGame(args[2], player)) {
            listTeams(args[2], player);
        }

        // Check command
        if (Util.isArgsIncorrectLength(args, 4, "games team <list | add | spawn> <Game Name> <Team Name>", player))
            return;
        if (Util.isInvalidGame(args[2], player)) return;

        // Check if teams exist
        if (Util.getData(args[2]).teamPlayerList == null) {
            player.sendMessage(ChatColor.RED + "Teams not enabled for " + args[2]);
            return;
        }

        // Run team command
        if (args[1].equals("spawn")) {
            setTeamSpawn(args[2], args[3], player);
        } else if (args[1].equals("add")) {
            addTeam(args[2], args[3], player);
        }

    }

    public static void listTeams(String gameName, Player player) {
        player.sendMessage(ChatColor.AQUA + gameName + " Teams: ");
        int i = 0;
        for (String s : Util.getData(gameName).teamPlayerList.keySet()) {
            player.sendMessage(ChatColor.GREEN + Integer.toString(i) + ". " + s);
        }
        return;
    }

    public static void setTeamSpawn(String gameName, String teamName, Player player) {
        Location location = player.getLocation();
        Util.getData(gameName).teamStartPositions.put(teamName, location);
    }

    public static void addTeam(String gameName, String teamName, Player player) {
        GameData data = Util.getData(gameName);

        if (data.teamPlayerList.containsKey(teamName)) {
            player.sendMessage(ChatColor.RED + teamName + " already exists!");
        }
    }

    /**
     * Player runs arena command
     *
     * @param player player that issued the command
     * @param args   command arguments
     */
    public static void arenaCommand(Player player, String[] args) {
        // Check command
        if (Util.isArgsIncorrectLength(args, 3, "games arena <save | load> <Game Name>", player)) return;
        if (Util.isInvalidGame(args[2], player)) return;

        // Get game data
        GameData data = Util.getData(args[2]);

        // Check to see if arena is enabled
        if (data.pos1 == null || data.pos2 == null) {
            player.sendMessage(ChatColor.RED + "Game " + data.name + " doesn't have arena enabled");
            return;
        }

        // Check operation and run method if valid
        if (args[1].equals("save"))
            saveArena(player, data);
        else if (args[1].equals("load"))
            loadArena(data);
        else {
            player.sendMessage(ChatColor.RED + "Unknown operation" + args[2]);
            player.sendMessage(ChatColor.RED + "Usage: /games arena <save | load> <Game Name>");
        }
    }

    /**
     * Saves the arena
     *
     * @param player player that issued the command
     * @param data   the game object base class
     */
    public static void saveArena(Player player, GameData data) {
        // TODO: update corners when saving
        // Get the region object from position 1 and 2
        CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(MikeyMinigames.data.currWorld), data.pos1, data.pos2);
        data.arenaSaved = true;

        // Tell the player that we're saving
        player.sendMessage(ChatColor.AQUA + "Saving " + region.getArea() + " blocks...");

        // Create clipboard and editsession from region and copy it
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);
        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        try {
            Operations.complete(forwardExtentCopy);
            editSession.flushSession();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }

        // Write clipboard to the save file
        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(Util.getFileInDir(data.gameFolder, data.name + ".arena")))) {
            try {
                writer.write(clipboard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set the arenaSaved flag to true and tell the player that the arena was saved
        player.sendMessage(ChatColor.GOLD + "Saved!");

        // Saves the game
        saveGame(data.name);
    }

    /**
     * Loads the arena
     *
     * @param data the game object base class
     */
    public static void loadArena(GameData data) {
        // Create clipboard from file
        File gameFile = Util.getFileInDir(data.gameFolder, data.name + ".arena");
        ClipboardFormat format = ClipboardFormats.findByFile(gameFile);

        // Reads the schematic and pastes
        try (ClipboardReader reader = format.getReader(new FileInputStream(gameFile))) {
            Clipboard clipboard = reader.read();
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(MikeyMinigames.data.currWorld), -1);
            double x = data.pos1.getX();
            double y = data.pos1.getY();
            double z = data.pos1.getZ();
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(x, y, z))
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            editSession.flushSession();
        } catch (IOException | WorldEditException e) {
            e.printStackTrace();
        }
    }

    public static void playerDataFunction(String function, GameData data) {
        if (function.equals("save")) {
            // Create player folder if doesn't exist
            data.playerFolder = new File(Util.getSubPath(data.gameFolder, "playerData"));
            if (!data.playerFolder.exists()) data.gameFolder.mkdir();

            try {
                // Open players file
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Util.getFileInDir(data.playerFolder, "players.dat"))));

                // Write all data to player's data file
                for (Player p : data.gamePlayers.values()) {
                    out.println(p.getUniqueId().toString());
                    writePlayerData(p, data);
                }

                // Close players file
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Get player folder
            data.playerFolder = new File(Util.getSubPath(data.gameFolder, "playerData"));

            // Check if folder exists
            if (!data.playerFolder.exists()) return;

            // Instantiate hashmap
            data.playerOldScores = new HashMap<>();

            try {
                BufferedReader f = new BufferedReader(new FileReader(Util.getFileInDir(data.playerFolder, "players.dat")));
                String id;
                while ((id = f.readLine()) != null) {
                    Player p = Bukkit.getPlayer(UUID.fromString(id));
                    if (p != null)
                        data.playerOldScores.put(p.getName(), loadPlayerData(id, data));
                }
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void writePlayerData(Player player, GameData data) throws IOException {
        // Create player file
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Util.getFileInDir(data.playerFolder, player.getUniqueId().toString()))));

        // Write score to file
        long[] scores = data.playerScores.get(player.getName());
        out.println(scores.length);
        for (long i : scores)
            out.println(i);

        // Close writer
        out.close();
    }

    public static long[] loadPlayerData(String id, GameData data) throws IOException {
        // Create file reader
        BufferedReader f = new BufferedReader(new FileReader(Util.getFileInDir(data.playerFolder, id)));

        // Get num of lines and create array
        int n = Integer.parseInt(f.readLine());
        long[] lines = new long[n];

        // Iterate through lines and store values in the array
        for (int i = 0; i < n; i++) {
            lines[i] = Long.parseLong(f.readLine());
        }

        // Close reader
        f.close();

        // Return array
        return lines;
    }

    /**
     * Runs the save command on all games and creates a global games json file to reference
     *
     * @param sender player/console that issued the command
     */
    public static void saveAllGames(CommandSender sender) {
        // Create output array
        JSONArray out = new JSONArray();

        // Iterate through games, add game name to output array and run saveGame method
        for (String str : MikeyMinigames.data.gameData.keySet()) {
            out.add(str);
            saveGame(str);
            sender.sendMessage(ChatColor.GOLD + str + ChatColor.AQUA + " was saved successfully");
        }

        // Write output array to games json file
        try {
            FileWriter fw = new FileWriter(Util.getFileInDir(MikeyMinigames.data.gamesFolder, "games.json"));
            fw.write(out.toJSONString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Tell the player that the saving was successful
        sender.sendMessage(ChatColor.GOLD + "Games saved!");
    }

    /**
     * Saves single game to its own file in the games folder
     *
     * @param gameName the name of the game
     */
    public static void saveGame(String gameName) {
        // Get game data and create output object
        GameData data = Util.getData(gameName);

        // Write data to file
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Util.getFileInDir(data.gameFolder, gameName + ".dat"))));

            // Save name, type, enabled, and arena saved
            out.printf("%s %s %d %d\n", data.name, GameData.gameTypeToString(data.gameType),
                    Util.bti(data.enabled), Util.bti(data.arenaSaved));

            // Add info based on game type
            switch (data.gameType) {
                case SPLEEF:
                    out.println(Util.locationToOutString(data.lobby));
                    out.println(Util.locationToOutString(data.spectatorLoc));
                    out.println(Util.locationToOutString(data.exitLoc));
                    out.println(Util.blockVector3ToOutString(data.startPos1));
                    out.println(Util.blockVector3ToOutString(data.startPos2));
                    out.println(Util.blockVector3ToOutString(data.pos1));
                    out.println(Util.blockVector3ToOutString(data.pos2));
                    break;
                case BEDWARS:
                    out.println(Util.locationToOutString(data.lobby));
                    out.println(Util.locationToOutString(data.spectatorLoc));
                    out.println(Util.locationToOutString(data.exitLoc));
                    out.println(Util.stringLocationHashToOutString(data.teamStartPositions));
                    out.println(Util.blockVector3ToOutString(data.pos1));
                    out.println(Util.blockVector3ToOutString(data.pos2));
                    break;
                case PARKOUR:
                    out.println(Util.locationToOutString(data.exitLoc));
                    out.println(Util.locationArrayListToOutString(data.checkpoints));
//                    playerDataFunction("save", data); // TODO: add
                    break;
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs the load games command on all games and creates the game objects from data files
     *
     * @param sender the player that issued the command
     */
    public static void loadAllGames(CommandSender sender) {
        // Send loading message to player
        sender.sendMessage(ChatColor.AQUA + "Loading all games...");

        // Read the file
        String input = Util.readAllLines(Util.getFileInDir(MikeyMinigames.data.gamesFolder, "games.json"));

        // Return if the file doesn't exist
        if (input == null) {
            sender.sendMessage(ChatColor.RED + "Games file doesn't exist!");
            return;
        }

        if (input.equals("[]")) {
            sender.sendMessage(ChatColor.RED + "No games exist");
            return;
        }

        // Create JSON parser and parse the input into an array
        JSONParser parser = new JSONParser();
        JSONArray arr;
        try {
            arr = (JSONArray) parser.parse(input);

            // Iterate through the array and load the game
            for (Object o : arr.toArray()) {
                loadGame((String) o);
                sender.sendMessage(ChatColor.AQUA + "Loaded " + ChatColor.GOLD + o);
            }
            sender.sendMessage(ChatColor.AQUA + "Loaded all games!");
        } catch (ParseException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "No games found");
        }
    }

    /**
     * Loads single game from the games folder
     *
     * @param gameName the name of the game
     */
    public static void loadGame(String gameName) {
        try {
            // Open file for reading
            Bukkit.broadcastMessage(gameName);
            Bukkit.broadcastMessage(Util.getFileInDir(new File(Util.getSubPath(MikeyMinigames.data.gamesFolder, gameName)), gameName + ".dat").getAbsolutePath());
            BufferedReader f = new BufferedReader(new FileReader(Util.getFileInDir(new File(Util.getSubPath(MikeyMinigames.data.gamesFolder, gameName)), gameName + ".dat")));

            // Load general data
            StringTokenizer st = new StringTokenizer(f.readLine());
            GameData data = new GameData(st.nextToken(), GameData.stringToGameType(st.nextToken()));
            setDefaults(data);
            data.enabled = Util.itb(st.nextToken());
            data.arenaSaved = Util.itb(st.nextToken());

            // Load data based on game type
            switch (data.gameType) {
                case SPLEEF:
                    data.lobby = Util.stringOutToLocation(f.readLine());
                    data.spectatorLoc = Util.stringOutToLocation(f.readLine());
                    data.exitLoc = Util.stringOutToLocation(f.readLine());
                    data.startPos1 = Util.stringOutToBlockVector3(f.readLine());
                    data.startPos2 = Util.stringOutToBlockVector3(f.readLine());
                    data.pos1 = Util.stringOutToBlockVector3(f.readLine());
                    data.pos2 = Util.stringOutToBlockVector3(f.readLine());
                    break;
                case BEDWARS:
                    data.lobby = Util.stringOutToLocation(f.readLine());
                    data.spectatorLoc = Util.stringOutToLocation(f.readLine());
                    data.exitLoc = Util.stringOutToLocation(f.readLine());
                    data.teamStartPositions = Util.stringOutToStringLocationHash(f.readLine());
                    data.pos1 = Util.stringOutToBlockVector3(f.readLine());
                    data.pos2 = Util.stringOutToBlockVector3(f.readLine());
                    break;
                case PARKOUR:
                    data.exitLoc = Util.stringOutToLocation(f.readLine());
                    data.checkpoints = Util.stringOutToLocationArrayList(f.readLine());
//                    playerDataFunction("load", data); // TODO: add
                    break;
            }

            // Close reader
            f.close();

            // Add game data to games hashmap
            MikeyMinigames.data.gameData.put(data.name, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
