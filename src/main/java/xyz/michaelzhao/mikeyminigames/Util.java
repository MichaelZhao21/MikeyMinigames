package xyz.michaelzhao.mikeyminigames;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import xyz.michaelzhao.mikeyminigames.games.GameData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

public class Util {
    /**
     * Returns the subfile path given a directory and filename
     *
     * @param parent  the parent directory
     * @param subName the file name
     * @return String representing the file path
     */
    public static String getSubPath(File parent, String subName) {
        return parent.getPath() + System.getProperty("file.separator") + subName.replace(' ', '_');
    }

    /**
     * Opens the game file
     *
     * @param parentFolder the parent directory of the file
     * @param gameName     the name of the game
     * @return the file object
     */
    public static File getFileInDir(File parentFolder, String gameName) {
        // Creates a file object, replacing spaces with underscores in the game name
        File gameFile = new File(getSubPath(parentFolder, gameName));

        // Check to see if the file exists and creates one if not
        if (!gameFile.exists()) {
            try {
                gameFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Returns the file object
        return gameFile;
    }

    /**
     * Parse all lines of a file and compile it into a string
     * for use by a JSONParser object
     *
     * @param path the path of the file
     * @return String of concatenated file text
     */
    public static String readAllLines(File path) {
        try {
            BufferedReader f = new BufferedReader(new FileReader(path));
            String line;
            StringBuilder in = new StringBuilder();
            while ((line = f.readLine()) != null) {
                in.append(line);
            }
            f.close();
            return in.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Recursively deletes a directory
     * @param directoryToBeDeleted the directory to be deleted
     * @return if the deletion was successful
     */
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    /**
     * Checks the length of the arguments and prints the message to the player if incorrect
     *
     * @param args          command arguments
     * @param correctLength correct length of arguments
     * @param usage         usage message to send player
     * @param sender        the sender that issued the command
     * @return if the argument was incorrect or not
     */
    public static boolean isArgsIncorrectLength(String[] args, int correctLength, String usage, CommandSender sender) {
        if (args.length != correctLength) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + usage);
            return true;
        }
        return false;
    }

    /**
     * Checks to see if the game is invalid and prints the message to the player if invalid
     *
     * @param gameName name of the game
     * @param sender   the sender that issued the command
     * @return if the game was invalid or not
     */
    public static boolean isInvalidGame(String gameName, CommandSender sender) {
        if (!MikeyMinigames.data.gameData.containsKey(gameName.toLowerCase())) {
            sender.sendMessage(ChatColor.RED + "Game " + gameName + " could not be found!");
            return true;
        }
        return false;
    }

    public static BlockVector3 locationToBlockVector3(Location location) {
        return BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }

    /**
     * Changes a Location object to a formatted string
     *
     * @param label the label to put in front of the coordinate string
     * @param l     the Location object
     * @return the formatted string
     */
    public static String locationToString(String label, Location l) {
        Vector v = l.getDirection();
        return String.format("%s: (%.2f, %.2f, %.2f) | facing <%.2f, %.2f, %.2f>", label, l.getX(), l.getY(), l.getZ(), l.getX(), l.getY(), l.getZ());
    }

    public static String locationToFloorString(String label, Location l) {
        Vector v = l.getDirection();
        return String.format("%s: (%f, %f, %f) | facing <%.2f, %.2f, %.2f>", label, Math.floor(l.getX()), Math.floor(l.getY()), Math.floor(l.getZ()), l.getX(), l.getY(), l.getZ());
    }

    public static String blockVector3ToString(String label, BlockVector3 b) {
        return String.format("%s: (%d, %d, %d)", label, b.getX(), b.getY(), b.getZ());
    }

    public static String stringBlockVector3hashToString(String label, HashMap<String, BlockVector3> map) {
        StringBuilder sb = new StringBuilder(label).append(":");
        for (String s : map.keySet())
            sb.append("\n    ").append(blockVector3ToString(s, map.get(s)));
        return sb.toString();
    }

    public static String stringLocationHashToString(String label, HashMap<String, Location> map) {
        StringBuilder sb = new StringBuilder(label).append(":");
        for (String s : map.keySet())
            sb.append("\n    ").append(locationToString(s, map.get(s)));
        return sb.toString();
    }

    public static String locationArrayListToString(String label, ArrayList<Location> arr) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Location l : arr) {
            sb.append("\n    ").append(locationToString(Integer.toString(count), l));
            count++;
        }
        return String.format("%s:[%s]", label, sb.toString());
    }

    public static String blockVector3ArrayListToString(String label, ArrayList<BlockVector3> arr) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (BlockVector3 b : arr) {
            sb.append("\n    ").append(blockVector3ToString(Integer.toString(count), b));
            count++;
        }
        return String.format("%s:%s", label, sb.toString());
    }

    public static String intArrayToString(String label, int[] arr) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i : arr) {
            if (!first) sb.append(", ");
            sb.append(i);
            first = false;
        }
        return String.format("%s:[%s]", label, sb.toString());
    }

    public static String longArrayToString(String label, long[] arr) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (long i : arr) {
            if (!first) sb.append(", ");
            sb.append(i);
            first = false;
        }
        return String.format("%s:[%s]", label, sb.toString());
    }

    public static String stringIntArrayHashToString(String label, HashMap<String, int[]> map) {
        StringBuilder sb = new StringBuilder(label).append(":");
        for (String s : map.keySet())
            sb.append("\n    ").append(intArrayToString(s, map.get(s)));
        return sb.toString();
    }

    public static String stringLongArrayHashToString(String label, HashMap<String, long[]> map) {
        StringBuilder sb = new StringBuilder(label).append(":");
        for (String s : map.keySet())
            sb.append("\n    ").append(longArrayToString(s, map.get(s)));
        return sb.toString();
    }

    /**
     * Tests if the location is in default still (0, 0, 0)
     *
     * @param l Location object
     * @return if location is not set
     */
    public static boolean isLocationNotSet(Location l) {
        return l.getX() == 0 && l.getY() == 0 && l.getZ() == 0;
    }

    // TODO: Add javadoc
    public static GameData getData(String gameName) {
        return MikeyMinigames.data.gameData.get(gameName.toLowerCase());
    }

    public static ItemStack createInventoryItem(Material type, int amount, String name, String... lore) {
        // Create item w/ type and amt
        ItemStack item = new ItemStack(type);
        item.setAmount(amount);

        // Set meta
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        // Return the item
        return item;
    }

    public static ItemStack getToggle(Material material, String name, boolean value) {
        return Util.createInventoryItem(material, 1, name, bts(value));
    }

    /**
     * Boolean to String :D
     *
     * @param b the boolean
     * @return string representation of the boolean
     */
    public static String bts(boolean b) {
        return b ? "true" : "false";
    }

    public static int bti(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean itb(String s) {
        return s.equals("1");
    }

    public static boolean itb(char c) {
        return c == '1';
    }

    public static String locationToOutString(Location l) {
        Vector v = l.getDirection();
        return String.format("%f %f %f %f %f %f", l.getX(), l.getY(), l.getZ(), v.getX(), v.getY(), v.getZ());
    }

    public static Location stringOutToLocation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        Location l = new Location(MikeyMinigames.data.currWorld, Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
        Vector v = new Vector(Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
        l.setDirection(v);
        return l;
    }

    public static String blockVector3ToOutString(BlockVector3 b) {
        return String.format("%d %d %d", b.getX(), b.getY(), b.getZ());
    }

    public static BlockVector3 stringOutToBlockVector3(String s) {
        StringTokenizer st = new StringTokenizer(s);
        return BlockVector3.at(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
    }

    public static String stringBlockVector3hashToOutString(HashMap<String, BlockVector3> map) {
        StringBuilder out = new StringBuilder();

        for (String s : map.keySet())
            out.append(s).append(" ").append(blockVector3ToOutString(map.get(s))).append(" ");

        return String.format("%d %s", map.keySet().size(), out.toString());
    }

    public static HashMap<String, BlockVector3> stringOutToStringBlockVector3Hash(String s) {
        HashMap<String, BlockVector3> out = new HashMap<>();
        StringTokenizer st = new StringTokenizer(s);
        StringBuilder sb;
        int n = Integer.parseInt(st.nextToken());
        for (int i = 0; i < n; i++) {
            sb = new StringBuilder();
            sb.append(st.nextToken()).append(" ").append(st.nextToken()).append(" ").append(st.nextToken());
            out.put(st.nextToken(), stringOutToBlockVector3(sb.toString()));
        }
        return out;
    }

    public static String stringLocationHashToOutString(HashMap<String, Location> map) {
        StringBuilder out = new StringBuilder();

        for (String s : map.keySet())
            out.append(s).append(" ").append(locationToOutString(map.get(s))).append(" ");

        return String.format("%d %s", map.keySet().size(), out.toString());
    }

    public static HashMap<String, Location> stringOutToStringLocationHash(String s) {
        HashMap<String, Location> out = new HashMap<>();
        StringTokenizer st = new StringTokenizer(s);
        StringBuilder sb;
        int n = Integer.parseInt(st.nextToken());
        for (int i = 0; i < n; i++) {
            sb = new StringBuilder();
            sb.append(st.nextToken()).append(" ").append(st.nextToken()).append(" ").append(st.nextToken());
            out.put(st.nextToken(), stringOutToLocation(sb.toString()));
        }
        return out;
    }

    public static String stringIntArrayHashToOutString(HashMap<String, int[]> map) {
        StringBuilder out = new StringBuilder();

        for (String s : map.keySet()) {
            out.append(s).append(' ')
                    .append(intArrayToOutString(map.get(s))).append('\n');
        }

        return String.format("%d\n%s", map.keySet().size(), out.toString());
    }

    public static HashMap<String, int[]> stringOutToStringIntArrHash(String s) {
        HashMap<String, int[]> out = new HashMap<>();
        StringTokenizer st = new StringTokenizer(s);
        StringBuilder sb;
        int n = Integer.parseInt(st.nextToken());
        for (int i = 0; i < n; i++) {
            sb = new StringBuilder();

            out.put(st.nextToken(), stringOutToIntArr(sb.toString()));
        }
        return out;
    }

    public static String intArrayToOutString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i : arr) {
            sb.append(i).append(" ");
        }
        return String.format("%d %s", arr.length, sb.toString());
    }

    public static int[] stringOutToIntArr(String s) {
        StringTokenizer st = new StringTokenizer(s);
        int n = Integer.parseInt(st.nextToken());
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = Integer.parseInt(st.nextToken());
        }
        return out;
    }

    public static String locationArrayListToOutString(ArrayList<Location> arr) {
        StringBuilder sb = new StringBuilder();
        for (Location l : arr) {
            sb.append(locationToOutString(l)).append(" ");
        }
        return String.format("%d %s", arr.size(), sb.toString());
    }

    public static ArrayList<Location> stringOutToLocationArrayList(String s) {
        StringTokenizer st = new StringTokenizer(s);
        StringBuilder sb;
        ArrayList<Location> out = new ArrayList<>();
        int n = Integer.parseInt(st.nextToken());
        for (int i = 0; i < n; i++) {
            sb = new StringBuilder();
            sb.append(st.nextToken()).append(" ").append(st.nextToken()).append(" ").append(st.nextToken())
                    .append(" ").append(st.nextToken()).append(" ").append(st.nextToken()).append(" ").append(st.nextToken());
            out.add(stringOutToLocation(sb.toString()));
        }
        return out;
    }

    public static String blockVector3ArrayListToOutString(ArrayList<BlockVector3> arr) {
        StringBuilder sb = new StringBuilder();
        for (BlockVector3 b : arr) {
            sb.append(blockVector3ToOutString(b)).append(" ");
        }
        return String.format("%d %s", arr.size(), sb.toString());
    }

    public static ArrayList<BlockVector3> stringOutToBlockVector3ArrayList(String s) {
        StringTokenizer st = new StringTokenizer(s);
        StringBuilder sb;
        ArrayList<BlockVector3> out = new ArrayList<>();
        int n = Integer.parseInt(st.nextToken());
        for (int i = 0; i < n; i++) {
            sb = new StringBuilder();
            sb.append(st.nextToken()).append(" ").append(st.nextToken()).append(" ").append(st.nextToken());
            out.add(stringOutToBlockVector3(sb.toString()));
        }
        return out;
    }

    public static String formatTime(long millis) {
        return String.format("%02d mins %02d secs",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static int findLocationInArrList(ArrayList<Location> arr, Location loc) {
        for (int i = 0; i < arr.size(); i++) {
            if (compLocations(arr.get(i), loc)) return i;
        }
        return -1;
    }

    public static boolean compLocations(Location loc1, Location loc2) {
        return (Math.floor(loc1.getX()) == Math.floor(loc2.getX()) &&
                Math.floor(loc1.getY()) == Math.floor(loc2.getY()) &&
                Math.floor(loc1.getZ()) == Math.floor(loc2.getZ()));

    }
}
